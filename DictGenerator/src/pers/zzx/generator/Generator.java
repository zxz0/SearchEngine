package pers.zzx.generator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class Generator {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		
		// Get data directory from command line 
		if (args.length != 1) {
			System.out.println("Input HTML directory as an argument. eg. java Generator /User/username/data");
			return;
		}
		String direc = args[0];
		
    	final Path path = Paths.get(direc);
    	final MutableInt i = new MutableInt(0);
    	final PrintWriter writer = new PrintWriter("big.txt", "UTF-8");
    	writer.print("politics election Brexit NASDAQ NBA Snapchat Illegal Immingration Donald Trump Russia NASA");
    	
    	SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>() {
    	    @Override
    	    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    	    	// Get parameters
    	        BodyContentHandler handler = new BodyContentHandler(-1);	// char limit: -1 unlimited
    	        Metadata metadata = new Metadata();
    	        FileInputStream inputstream = new FileInputStream(file.toFile());
    	        ParseContext pcontext = new ParseContext();
    	        
    	        // HTML parser 
    	        HtmlParser htmlparser = new HtmlParser();
    	        try {
    				htmlparser.parse(inputstream, handler, metadata, pcontext);
    				// get letter content without continuing whitespace
    				String content = handler.toString().trim().replaceAll("[^a-zA-Z]", " ").replaceAll("\\s+", " ");
    				// write to file
    				writer.print(content + " ");
    				
    				// Count and log
    				i.add(1);
        	        String fileName = file.toString();
        	    	System.out.println(i.toString() + ": " + fileName.substring(fileName.lastIndexOf("/") + 1));
    			} catch (SAXException e) {
    				e.printStackTrace();
    			} catch (TikaException e) {
    				e.printStackTrace();
    			}
    	        return super.visitFile(file, attrs);
    	    }
    	};
    	
    	// Walk through the directory
    	java.nio.file.Files.walkFileTree(path, finder);

    	writer.close();

    	System.out.println(i.toString() + " entries: finished!");
	}
}