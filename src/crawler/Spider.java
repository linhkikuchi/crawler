package crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import data.Product;

/**
*
* @author Linh
*/

public class Spider {
	// Fields
	final static Logger logger = Logger.getLogger(Spider.class);
	private static final int MAX_PAGES_TO_SEARCH = 50;
	private Set<String> pagesVisited = new HashSet<String>();
	private List<String> pagesToVisit = new LinkedList<String>();

	private String nextUrl() {
		String nextUrl;
		do {
			nextUrl = this.pagesToVisit.remove(0);
		} while (this.pagesVisited.contains(nextUrl) && this.pagesToVisit.size() > 0);
		this.pagesVisited.add(nextUrl);
		return nextUrl;
	}

	public List<String> getpagesVisited() {
		return this.pagesToVisit;
	}

	public Product search(String url) throws IOException, ExecutionException {
		SpiderLeg leg = new SpiderLeg();
		Document doc = leg.crawl(url);
		Product prod = leg.searchItems(url, doc);
		this.pagesToVisit.addAll(leg.getAllLinks(doc, url));
		logger.info("***DONE* getting data from the input url");
		return prod;
	}

	public String searchMore(String url, String title) throws IOException, ExecutionException {
		SpiderLeg leg = new SpiderLeg();
		this.pagesVisited.add(url);
		String price = "-";
		// count the first entered url
		while (this.pagesVisited.size() < MAX_PAGES_TO_SEARCH) {
			String currentUrl = "";
			if (!this.pagesToVisit.isEmpty())
				currentUrl = this.nextUrl();
			// stop crawling if only first url is left
			if (currentUrl.equals(url) || currentUrl.equals(""))
				break;
			Document doc = leg.crawl(currentUrl);
			if (doc != null)
				price = leg.searchProductPriceOneMore(title, leg.getFormattedContent(doc));
			if (!price.equals("-")) {//break if find the price
				logger.debug(price);
				break;
			}
		}
		logger.info(String.format("**Done** Visited %s web page(s)", this.pagesVisited.size() + 1));
		return price;
	}

}
