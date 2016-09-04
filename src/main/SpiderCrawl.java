package main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Logger;
import crawler.*;
import data.Product;

/**
*
* @author Linh
*/

public class SpiderCrawl {
	final static Logger logger = Logger.getLogger(SpiderCrawl.class);
	static boolean firstRun = true;
	//name of the output directory to write items to
	final static String dir = "output";
	/**
	 * This is the test. It creates a spider (which creates spider legs) and
	 * crawls the web.
	 * 
	 * @throws IOException
	 * @throws ExecutionException
	 * 
	 */
	public static void main(String[] args) throws IOException, ExecutionException {
		//String url = "http://www.sli-demo.com/home-decor/electronics/madison-rx3400.html";
		String url=""; String answer="y";
		BufferedReader bufferReader;
		while(answer.equals("Y")|| answer.equals("y"))
		{
		System.out.println("Please enter the url to search : ");
		bufferReader = new BufferedReader(new InputStreamReader(System.in));
		url = bufferReader.readLine();
        System.out.println(String.format("Fetching %s...", url));
		Spider spider = new Spider();
		Product prod = spider.search(url);
		String price = prod.getPrice();
		if (price.equals("-")) {
			//if cannot find price, crawl more pages to find price
			price = spider.searchMore(url, prod.getProductTitle());
			prod.setPrice(price);
		}	
		logger.info(prod.toString());
		if(firstRun) 
			writeOutput("output.txt", prod.toString());
		else appendOutput("output.txt", prod.toString());
		System.out.println("Continue(Y/N)");
		answer =new BufferedReader(new InputStreamReader(System.in)).readLine();
		firstRun = false;
		} 
		
  
	}
	
	//write prod details to output file for first run
	public static void writeOutput(String fileName, String content){
		File directory = new File(dir);
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	    //write the content to file
		try(PrintStream ps = new PrintStream(dir+"/"+fileName)) 
		{ 
			ps.println(content); 
			ps.flush();
			ps.close();
		} 
		catch (IOException ioe) {
			logger.error("ERROR - Not able to write to file");
		}
	}
	
	//append prod details to output file for next url
	public static void appendOutput(String fileName, String content) {
		File aFile = new File(dir+"/" + fileName);
		try (PrintStream p = new PrintStream(new BufferedOutputStream(new FileOutputStream(aFile, true)));) {
			logger.debug("Append new item to file");
			p.println(content);
			p.close();
		} catch (IOException ioe) {
			logger.error("ERROR - Not able to write to file");
		}
	}
}
