package crawler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import data.Product;

/**
*
* @author Linh
*/

public class SpiderLeg {
	// Use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
	final static Logger logger = Logger.getLogger(SpiderLeg.class);
	/**
	 * This performs all the work. It makes an HTTP request, checks the
	 * response, and then gathers up all the links on the page. 
	 * 
	 * @param url
	 *            - The URL to visit
	 * @return HtmlDocument obj
	 */
	public Document crawl(String url) {
		String fileName = formatFileName(url);
		Document doc = new Document(fileName);
		try {			
			if(!checkFileExist(fileName)){
				//write html to file
				Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
				Connection.Response resp = connection.execute();
				if (resp.statusCode() == 200) {
					logger.debug("\n**Visiting** Received web page at " + url);
					doc = connection.get();
					writeToFile(fileName, doc.html());
					//try(PrintStream ps = new PrintStream("download/"+fileName)) { ps.println(doc.html()); } 
				} else {
					logger.error("**Failure** Retrieved something other than HTML");
				}			
			}
			else {
				String baseUrl= this.getBaseUrl(url);
				File input = new File("download/"+fileName);
				doc = Jsoup.parse(input, "UTF-8", baseUrl);		
			}
		} catch (Exception e) {
			logger.error("ERROR - Not able to check file");
		}
		return doc;
	}
	
	/**
	 * create dir download if dir doesn't exist, create it
	 * write html to the dir
	 * @param fileName
	 * @param content
	 */
	public void writeToFile(String fileName, String content){
		File directory = new File("download");
	    if (! directory.exists()){
	        directory.mkdir();
	    }
		try(PrintStream ps = new PrintStream("download/"+fileName)) { ps.println(content); } 
		catch (IOException ioe) {
			logger.error("ERROR - Not able to write to file");
		}
	}
	public String getFormattedContent(Document doc) {
		String pageContent = doc.toString();
		return pageContent.replaceAll("[^\\S\\r]+", " ");
	}
	public String formatFileName(String url){
		String f = url.replaceAll("http://.*?/", "");
		return f.replaceAll("/", "");
	}
	
	public boolean checkFileExist(String name) {
		File[] files = new File("download").listFiles();
		// If this pathname does not denote a directory, then listFiles()
		// returns null.
		if (files != null) {
			for (File file : files) {
				if (file.isFile() && name.equals(file.getName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * to get all links from input url with filter 
	 * 
	 */
	public List<String> getAllLinks(Document doc, String url){
		List<String> links = new LinkedList<String>();
		Elements linksOnPage = doc.select("a[href]");
		logger.debug("Found (" + linksOnPage.size() + ") links");
		String baseUrl= this.getBaseUrl(url);
		//only add links that has the same base url
		for (Element link : linksOnPage) {
			String newUrl = link.absUrl("href");
			if(newUrl.startsWith(baseUrl) && !newUrl.contains("#") 
					&& !newUrl.equals(url) && newUrl.endsWith(".html"))
				links.add(link.absUrl("href"));
		}
		return links;
	}

	public Product searchItems(String url, Document doc) throws IOException {
		if (doc == null) {
			logger.info("doc is null");
			return null;
		}
		Product prod = new Product(url);
		prod.setProductTitle(doc.title());
		prod.setShortDesc(this.searchShortDesc(this.getFormattedContent(doc)));
		prod.setAddInfo(this.searchProductAddInfo(doc));
		prod.setPrice(this.searchProductPrice(doc.title(), this.getFormattedContent(doc)));
		//logger.debug(prod.toString());
		return prod;
	}

	/**
	 * match html pattern of input url to get price from class price-info
	 */
	public String searchProductPrice(String title, String pageContent) {
		StringBuffer regex = new StringBuffer();
		regex.append(title);
		regex.append(
				"</span> </div> <div class=\"price-info\"> <div class=\"price-box\"> <span class=\"regular-price\".*?<span class=\"price\">(.*?)</span>");
		Pattern pattern = Pattern.compile(regex.toString());
		Matcher matcher = pattern.matcher(pageContent);
		if (matcher.find()) {
			return removePreSpaces(matcher.group(1));
		} else {
			return "-";
		}
	}

	/**
	 * match html pattern to get price from other pages (if price cannot be found from input page)
	 */
	public String searchProductPriceOneMore(String title, String pageContent) {
		StringBuffer regex = new StringBuffer();
		regex.append(title);
		regex.append(
				"</a></h3> <div class=\"price-box\"> <span class=\"regular-price\".+?<span class=\"price\">(.*?)</span>");
		Pattern pattern = Pattern.compile(regex.toString());
		Matcher matcher = pattern.matcher(pageContent);
		if (matcher.find()) {
			System.out.println(matcher.group(1));
			return removePreSpaces(matcher.group(1));
		} else {
			return "-";
		}
	}

	/**
	 * match html pattern to get short description
	 */
	public String searchShortDesc(String pageContent) {
		Pattern pattern = Pattern.compile("<div class=\"short-description\"> <div class=\"std\">(.+?)</div>");
		Matcher matcher = pattern.matcher(pageContent);
		if (matcher.find()) {
			return removePreSpaces(matcher.group(1));
		} else {
			return "-";
		}
	}

	/**
	 * to get additional information from table id=product-attribute-specs-table
	 */
	public String searchProductAddInfo(Document doc) throws IOException {
		if (doc.html().contains("product-attribute-specs-table")) {
			Elements tableHeader = doc.getElementById("product-attribute-specs-table")
					.select("tr");
			StringBuffer addInfo = new StringBuffer();
			for (int i = 0; i < tableHeader.size(); i++) {
				String row = tableHeader.get(i).text();
				if (i < tableHeader.size() - 1) {
					addInfo.append(row);
					addInfo.append(", ");
				} else {
					addInfo.append(row);
				}
			}
			return addInfo.toString();
		}
		return "-";
	}

	private String removePreSpaces(String inputStr) {
		if (inputStr != null && !"".equals(inputStr)) {
			for (int i = 0; i < inputStr.length(); i++) {
				if (!" ".equals(String.valueOf(inputStr.charAt(i)))) {
					return inputStr.substring(i);
				}
			}
		}
		return "";
	}
	
	/**
	 * to get base url from the input url
	 */
	private String getBaseUrl(String url) {
		String baseUrl="http://";
		Pattern pattern = Pattern.compile("http://(.*?)/.+?/*.html"); 
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
        	baseUrl = baseUrl.concat(matcher.group(1));
        }
        return baseUrl;
	}

}