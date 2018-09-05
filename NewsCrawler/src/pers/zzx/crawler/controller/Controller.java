package pers.zzx.crawler.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import pers.zzx.crawler.model.*;

public class Controller {
	public static void main(String[] args) throws Exception {
		Logger logger = LoggerFactory.getLogger(Controller.class);
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        logger.info("Starts at: {}", dtf.format(now));
		
		String root = System.getProperty("user.dir");
		
		// Set the configurations
		String crawlStorageFolder = Paths.get(root, "data").toString();
        int numberOfCrawlers = 1; 	// 7
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(16);
        config.setMaxPagesToFetch(10); 	// 20000
        config.setPolitenessDelay(300);
//        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36";
//        config.setUserAgentString(userAgent);
        config.setIncludeBinaryContentInCrawling(true); 	// binary: pdf, image...
        boolean resumable = false;
        // Clear data directory if not resumable 
        if (resumable) {
        	// TODO: for internal data: somehow store and restore from temporary file?
        	config.setResumableCrawling(true);
        } else {
    		File dataFolder = new File(Paths.get(root, "data", "contents").toString());
    		if (dataFolder.exists() && !dataFolder.delete()) {
    			String[] entries = dataFolder.list();	// assume no sub directory: no need to recursively delete
    			for(String s: entries) {
    				File currentFile = new File(dataFolder.getPath(), s);
    				currentFile.delete();
    			}
    			dataFolder.delete();
    		}
    		logger.info("Data directory: {} cleared!", dataFolder.getPath());
        }
        
        // Instantiate the controller for this crawl
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		
		String seed = "http://www.nytimes.com/";
		
		controller.addSeed(seed);
		
        controller.start(MyCrawler.class, numberOfCrawlers);
        
        
        List<Object> crawlersLocalData = controller.getCrawlersLocalData();
        List<Site> totalResults = new LinkedList<Site>();
        List<String> totalOutUrls = new LinkedList<String>();
        Hashtable<String, Integer> totalStatusCodeCounts = new Hashtable<String, Integer>();
        Hashtable<String, Integer> totalContentTypeCounts = new Hashtable<String, Integer>();
        Hashtable<String, Integer> totalFileSizeCategoryCounts = new Hashtable<String, Integer>();
        int numSucceeded = 0;
        
        totalFileSizeCategoryCounts.put("< 1KB", 0);
        totalFileSizeCategoryCounts.put("1KB ~ <10KB", 0);
        totalFileSizeCategoryCounts.put("10KB ~ <100KB", 0);
        totalFileSizeCategoryCounts.put("100KB ~ <1MB", 0);
        totalFileSizeCategoryCounts.put(">= 1MB", 0);
        
        // Collect results from every thread
        for (Object localData: crawlersLocalData) {
            CrawlStat stat = (CrawlStat) localData;
            List<Site> results = stat.getResults();
            List<String> outUrls = stat.getUrls();
            Hashtable<String, Integer> statusCodeCount = stat.getStatusCodeCount();
            Hashtable<String, Integer> contentTypeCount = stat.getCountentTypeCount();
            Hashtable<String, Integer> fileSizeCategoryCount = stat.getFileSizeCategoryCount();
            
            numSucceeded += stat.getNumProcessedPage();
            
            totalResults.addAll(results);
            results.clear();
            
            totalOutUrls.addAll(outUrls);
            outUrls.clear();
            
            Iterator<Map.Entry<String, Integer>> iterSCC = statusCodeCount.entrySet().iterator();
            while(iterSCC.hasNext()) {
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iterSCC.next();
                String statusCodeAndDescription = (String)entry.getKey();
                int count = (Integer)entry.getValue();
                totalStatusCodeCounts.put(statusCodeAndDescription, totalStatusCodeCounts.containsKey(statusCodeAndDescription) ? (totalStatusCodeCounts.get(statusCodeAndDescription) + count) : count);
            }
//            statusCodeCount.clear();
            
            Iterator<Map.Entry<String, Integer>> iterCTC = contentTypeCount.entrySet().iterator();
            while(iterCTC.hasNext()) {
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iterCTC.next();
                String countentType = (String)entry.getKey();
                int count = (Integer)entry.getValue();
                totalContentTypeCounts.put(countentType, totalContentTypeCounts.containsKey(countentType) ? (totalContentTypeCounts.get(countentType) + count) : count);
            }
//            contentTypeCount.clear();
            
            Iterator<Map.Entry<String, Integer>> iterFSCC = fileSizeCategoryCount.entrySet().iterator();
            while(iterFSCC.hasNext()) {
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iterFSCC.next();
                String fileSizeCategory = (String)entry.getKey();
                int count = (Integer)entry.getValue();
                totalFileSizeCategoryCounts.put(fileSizeCategory, totalFileSizeCategoryCounts.get(fileSizeCategory) + count);
            }
//            fileSizeCategoryCount.clear();
        }
        
//        int numSucceeded = totalStatusCodeCounts.get("200 OK");
        
        // Write results to files
        try {
        	Path statisticsFolder = Paths.get(root, "data", "statistics");
        	Path contentsFolder = Paths.get(root, "data", "contents");
        	Files.createDirectories(statisticsFolder);
        	Files.createDirectories(contentsFolder);
        	
        	// Write fetched page info, visited page info, hash name - HTML file mapping
        	File fetchedInfo = new File(statisticsFolder.toString(), "fetch_NYTimes.csv");
        	File visitedInfo = new File(statisticsFolder.toString(), "visit_NYTimes.csv");
        	File csvMap = new File(contentsFolder.toString(), "mapNYTimesDataFile.csv");

        	BufferedWriter bwFetched = new BufferedWriter(new FileWriter(fetchedInfo, false));
        	BufferedWriter bwVisited = new BufferedWriter(new FileWriter(visitedInfo, false));
        	BufferedWriter bwMap = new BufferedWriter(new FileWriter(csvMap, false));
        	
        	logger.info("Writing to {}, {} and {}...", "fetch_NYTimes.csv", "visit_NYTimes.csv", "mapNYTimesDataFile.csv");
        	
            for (Site result : totalResults) {
            	int httpStatusCode = result.getHttpStatusCode();
            	bwFetched.write(result.getUrl().replace(",", "-") + "," + httpStatusCode); 
            	bwFetched.newLine();
                if (httpStatusCode == 200) {
                	bwVisited.write(result.getUrl().replace(",", "-") + "," + result.getSize() + ", " + result.getOutlinkNum() + ", " + result.getContentType());
                	bwVisited.newLine();
                	if (result.getContentType() == "text/html") {
                		bwMap.write(result.getHashedName() + "," + result.getUrl().replace(",", "-"));
                		bwMap.newLine();
                	}
                }
            }
            
            bwFetched.close();
        	bwVisited.close();
        	bwMap.close();
        	
        	int numAttempted = totalResults.size();
//        	totalResults.clear();
        	
        	// Write outgoing urls info
        	File outUrlInfo = new File(statisticsFolder.toString(), "urls_NYTimes.csv");
        	BufferedWriter bwOutUrl = new BufferedWriter(new FileWriter(outUrlInfo, false));
        	
        	// Calculate distinct total and within-domain count
        	HashSet<String> noDupOutUrl = new HashSet<String>();
        	int numUniqueWithin = 0;
        	
        	logger.info("Writing to {}...", "urls_NYTimes.csv");
        	for (String url : totalOutUrls) {
        		String inside = "N_OK";
        		if (url.startsWith("https://www.nytimes.com/") 
        				|| url.startsWith("http://www.nytimes.com/")) {
        				inside = "OK";
        				if (noDupOutUrl.add(url)) {
                			// this set did NOT already contain the specified element
        					numUniqueWithin++;
                		}
        		} else {
        			noDupOutUrl.add(url);
        		}
        		
        		bwOutUrl.write(url.replace(",", "-") + "," + inside); 
        		bwOutUrl.newLine();
        	}
        	
        	bwOutUrl.close();
        	
        	int numOutUrl = totalOutUrls.size();
//        	totalOutUrls.clear();
        	int numUniqueOutUrl = noDupOutUrl.size();
//        	noDupOutUrl.clear();	// obj = null;?
        	
        	
        	// Write statistics
        	File statisticsFile = new File(statisticsFolder.toString(), "CrawlReport_NYTimes.txt");
        	BufferedWriter bwStat = new BufferedWriter(new FileWriter(statisticsFile, false));
        	
        	logger.info("Writing to {}...", "CrawlReport_NYTimes.txt");
        	
        	bwStat.write("Fetch Statistics");
        	bwStat.newLine();
        	bwStat.write("================");
        	bwStat.newLine();
        	bwStat.write("# fetches attempted: " + numAttempted);
        	bwStat.newLine();
        	bwStat.write("# fetches succeeded: " + numSucceeded);
        	bwStat.newLine();
        	bwStat.write("# fetches aborted or failed: " + (numAttempted - numSucceeded));
        	bwStat.newLine();
        	bwStat.newLine();
        	
        	bwStat.write("Outgoing URLs:");
        	bwStat.newLine();
        	bwStat.write("==============");
        	bwStat.newLine();
        	bwStat.write("Total URLs extracted: " + numOutUrl);
        	bwStat.newLine();
        	bwStat.write("# unique URLs extracted: " + numUniqueOutUrl);
        	bwStat.newLine();
        	bwStat.write("# unique URLs within News Site: " + numUniqueWithin);
        	bwStat.newLine();
        	bwStat.write("# unique URLs outside News Site: " + (numUniqueOutUrl - numUniqueWithin));
        	bwStat.newLine();
        	bwStat.newLine();
        	
        	bwStat.write("Status Codes:");
        	bwStat.newLine();
        	bwStat.write("=============");
        	bwStat.newLine();
        	// Sort and traverse totalStatusCodesCounts 
        	TreeMap<String, Integer> sortedTotalStatusCodesCounts = new TreeMap<String, Integer>(totalStatusCodeCounts); 
        	Iterator<Map.Entry<String, Integer>> iterSCC = sortedTotalStatusCodesCounts.entrySet().iterator();
            while(iterSCC.hasNext()) {
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iterSCC.next();
                String statusCodeAndDescription = (String)entry.getKey();
                int count = (Integer)entry.getValue();
                bwStat.write(statusCodeAndDescription + ": " + count);
                bwStat.newLine();
            }
        	bwStat.newLine();
        	
        	bwStat.write("File Sizes:");
        	bwStat.newLine();
        	bwStat.write("===========");
        	bwStat.newLine();
        	bwStat.write("< 1KB: " + totalFileSizeCategoryCounts.get("< 1KB"));
        	bwStat.newLine();
        	bwStat.write("1KB ~ <10KB: " + totalFileSizeCategoryCounts.get("1KB ~ <10KB"));
        	bwStat.newLine();
        	bwStat.write("10KB ~ <100KB: " + totalFileSizeCategoryCounts.get("10KB ~ <100KB"));
        	bwStat.newLine();
        	bwStat.write("100KB ~ <1MB: " + totalFileSizeCategoryCounts.get("100KB ~ <1MB"));
        	bwStat.newLine();
        	bwStat.write(">= 1MB: " + totalFileSizeCategoryCounts.get(">= 1MB"));
        	bwStat.newLine();
        	bwStat.newLine();
        	
        	bwStat.write("Content Types:");
        	bwStat.newLine();
        	bwStat.write("==============");
        	bwStat.newLine();
        	// Traverse totalContentTypeCounts
            Iterator<Map.Entry<String, Integer>> iterCTC = totalContentTypeCounts.entrySet().iterator();
            while(iterCTC.hasNext()) {
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iterCTC.next();
                String countentType = (String)entry.getKey();
                int count = (Integer)entry.getValue();
                bwStat.write(countentType + ": " + count);
                bwStat.newLine();
            }
        	
        	bwStat.close();
        } catch (FileNotFoundException fnfe) {
        	logger.error(fnfe.getMessage());
        } catch (IOException ioe) {
        	logger.error(ioe.getMessage());
	    }
        
        now = LocalDateTime.now();
        logger.info("Finished at: {}", dtf.format(now));
	}
}