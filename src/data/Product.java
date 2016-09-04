package data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Setter;
import lombok.Getter;

/**
 *
 * @author Linh
 */
public class Product {

	@Setter
	@Getter
	private String productTitle;
	@Setter
	@Getter
	private String productUrl;
	@Setter
	private String brand;
	@Setter
	private String price;
	@Setter
	@Getter
	private String shortDesc;
	@Setter
	@Getter
	private String addInfo;

	private String categoryStructure;

	public Product(String productUrl) {
		this.productUrl = productUrl;
		this.setCategoryStructure();
	}

	public String getBrand() {
		if (brand == null || "".equals(brand)) {
			if (!"".equals(productTitle) && productTitle.indexOf(" ") != -1) {
				return productTitle.substring(0, productTitle.indexOf(" "));
			}
		}
		return "-";
	}

	public String getPrice() {
		return price == null || "".equals(price) ? "-" : price;
	}

	public void setCategoryStructure() {
		Pattern pattern = Pattern.compile("http://.+?/(.+?).html");
		Matcher matcher = pattern.matcher(this.productUrl);
		if (matcher.find()) {
			StringBuffer categoryStruts = new StringBuffer();
			categoryStruts.append("home/");
			String category = matcher.group(1);
			String formatted = category.replaceAll("-", " ");
			this.categoryStructure = categoryStruts.append(formatted).toString();
		}

	}

	public String getCategoryStructure() {
		return categoryStructure == null || "".equals(categoryStructure) ? "-" : categoryStructure;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Title:").append("\t\t\t");
		sb.append(this.getProductTitle()).append("\r\n");
		sb.append("Price:").append("\t\t\t");
		sb.append(this.getPrice()).append("\r\n");
		sb.append("Brand:").append("\t\t\t");
		sb.append(this.getBrand()).append("\r\n");
		sb.append("Additional information:").append("\t");
		sb.append(this.getAddInfo()).append("\r\n");
		sb.append("Short description:").append("\t");
		sb.append(this.getShortDesc()).append("\r\n");
		sb.append("Category structure:").append("\t");
		sb.append(this.getCategoryStructure()).append("\r\n");
		return sb.toString();
	}

}
