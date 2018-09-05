package pers.zzx.crawler.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import pers.zzx.crawler.model.*;

public class MyCrawler extends WebCrawler {
	final Logger logger = LoggerFactory.getLogger(MyCrawler.class);
	// Limit crawler so it only visits HTML, img, pdf
	private final static Pattern CTFILTER = Pattern.compile("^(?:text/html|image/gif|image/jpeg|image/png|application/pdf).*");	// ^: beginning; (?:): only group but no reverse index; .*: {0,} any char
	private final static Pattern URLFILTER = Pattern.compile(".*(\\.(css|js|json|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|rm|smil|wmv|swf|wma|zip|rar|gz|rss))$");

	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now;
	
	private final static String root = System.getProperty("user.dir"); 	//"/Users/zzx/Documents/workspace/SimpleWebCrawler/";
	
	CrawlStat myCrawlStat;

    public MyCrawler() {
        myCrawlStat = new CrawlStat();
    }
	
	/**
	* This method receives two parameters. The first parameter is the page
	* in which we have discovered this new url and the second parameter is
	* the new url. You should implement this function to specify whether
	* the given url should be crawled or not (based on your crawling logic).
	* In this example, we are instructing the crawler to ignore urls that
	* have css, js, git, ... extensions and to only accept urls that start
	* with "http://www.viterbi.usc.edu/". In this case, we didn't need the
	* referringPage parameter to make the decision.
	*/
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href =  url.getURL();
		String contentType = referringPage.getContentType();
//		logger.info("accessing: {}...", href);
		
		// Visit if inside domain, content type meets requirement
		if ((href.startsWith("https://www.nytimes.com/") || href.startsWith("http://www.nytimes.com/"))
				&& (contentType == null || CTFILTER.matcher(contentType).matches())
				&& !URLFILTER.matcher(href).matches()) { 	// Duplicate seems to be avoid by Crawler4j frontier db
//			logger.info("should visit, add to queue! {}", href);
			return true;
		} else {
//			logger.info("should NOT visit, omit! {}", href);
			return false;
		}
	}

	public void addFileSizeCategoryCount(long fileSize) {
		// Get file size category
		if (10000 <= fileSize && fileSize < 100000) {
			this.myCrawlStat.addOneToFileSizeCategoryCount("10KB ~ <100KB");
		} else if (100000 <= fileSize && fileSize < 1000000) {
			this.myCrawlStat.addOneToFileSizeCategoryCount("100KB ~ <1MB");
		} else if (1000000 <= fileSize) {
			this.myCrawlStat.addOneToFileSizeCategoryCount(">= 1MB");
		} else if (1000 <= fileSize)	{	// already < 10000 
			this.myCrawlStat.addOneToFileSizeCategoryCount("1KB ~ <10KB");
		} else {	// < 1000
			this.myCrawlStat.addOneToFileSizeCategoryCount("< 1KB");
		}
	}
	
	/**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
	@Override
	public void visit(Page page) {
	   String url = page.getWebURL().getURL();
//	   logger.info("Visiting: {}...", url);
	   Site site = new Site(url, 200);
	   
	   // text/html
	   if (page.getParseData() instanceof HtmlParseData) {
		   // Get info
	       HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
	       Set<WebURL> links = htmlParseData.getOutgoingUrls();
	       site.setOutlinkNum(links.size());
	       for(WebURL link: links){
	    	   this.myCrawlStat.addUrl(link.getURL());
			}
	       site.setContentType("text/html");
	       byte[] content = page.getContentData();
	       long fileSize = content.length;
	       site.setSize(fileSize);
	       this.addFileSizeCategoryCount(fileSize);
	       
	       // Save html
	       try {
	    	   String extension = ".html";
	    	   String hashedName = UUID.randomUUID() + extension;
		       Path outputFilePath = Paths.get(root, "data", "contents", hashedName);
		       Files.createDirectories(outputFilePath.getParent());
		       Files.createFile(outputFilePath);
	    	   Files.write(outputFilePath, content);
	    	   site.setHashedName(hashedName);
//	    	   logger.info("Visited and downloaded html: {}...", url);
	       } catch (IOException ioe) {
	    	   logger.error(ioe.getMessage());
	       }
	       this.myCrawlStat.addOneToStatusCodeCount("200 OK");
	       this.myCrawlStat.addOneToCountentTypeCount("text/html");
	       this.myCrawlStat.addResult(site);
	       this.myCrawlStat.addOneToProcessedPage();
	       
//	       logger.info("Visited and downloaded html: {}...", url);
	   } else if (page.getParseData() instanceof BinaryParseData) {		// image/gif|image/jpeg|image/png|application/pdf
	       // Chop encoding part for content type
		   String contentType = page.getContentType();
	       int pos = contentType.indexOf(";");
	       if (pos != -1) {
	    	   contentType = contentType.substring(0, pos);
	       }//	       str.split(" ")[0]
		   site.setContentType(contentType);
		   long fileSize = page.getContentData().length;
	       site.setSize(fileSize);
	       site.setOutlinkNum(0);
	       this.addFileSizeCategoryCount(fileSize);

	        /*
	        // get a unique name for storing this image
			String extension = url.substring(url.lastIndexOf('.'));
			String hashedName = UUID.randomUUID() + extension;
	        String filename = root + "data/contents/" + hashedName;
	        // store image
	        try {
	        	Files.write(page.getContentData(), new File(filename));
	            logger.info("Stored: {}", url);
	        } catch (IOException ioe) {
	            logger.error("Failed to write file: " + filename, ioe);
	        }
	        */
	       this.myCrawlStat.addOneToStatusCodeCount("200 OK");
	       this.myCrawlStat.addOneToCountentTypeCount(contentType);
	       this.myCrawlStat.addResult(site);
	       this.myCrawlStat.addOneToProcessedPage();
	       
//	       logger.info("Visited binary: {}...", url);
	   }	// other types: throw away
	   
	   // Dump statistics / log after processing every 50 pages for every thread
       if ((this.myCrawlStat.getNumProcessedPage() % 50) == 0) {
           this.dumpMyData();
       }
	}
	
	/**
     * This function is called by controller to get the local data of this crawler when job is
     * finished
     */
    @Override
    public Object getMyLocalData() {
        return this.myCrawlStat;
    }

    /**
     * This function is called by controller before finishing the job.
     * You can put whatever stuff you need here.
     */
   /* @Override
    public void onBeforeExit() {
        dumpMyData();
    }*/
    
    /**
     * This function is called once the header of a page is fetched. It can be
     * overridden by sub-classes to perform custom logic for different status
     * codes. For example, 404 pages can be logged, etc.
     *
     * @param webUrl WebUrl containing the statusCode
     * @param statusCode Html Status Code number
     * @param statusDescription Html Status COde description
     */
    @Override
    protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
    	String url = webUrl.getURL();
    	
//    	logger.info("attempt to fetch: {}, get status code {}: {}", url, statusCode, statusDescription);
    	
    	if (statusCode != 200) {
    		// Process the failed/aborted here, 200 ones later with more info
    		this.myCrawlStat.addOneToStatusCodeCount(statusCode + " " + statusDescription);
    		this.myCrawlStat.addResult(new Site(url, statusCode));
    	}
    }

    // log
    public void dumpMyData() {
        int id = getMyId();
        logger.info("Crawler {} > Processed Pages: {}", id, myCrawlStat.getNumProcessedPage());
        logger.info("Crawler {} > Total Links Found: {}", id, myCrawlStat.getTotalLinks());
        now = LocalDateTime.now();
        logger.info(dtf.format(now)); 	// YYYY/MM/DD HH:MM:SS
    }
}