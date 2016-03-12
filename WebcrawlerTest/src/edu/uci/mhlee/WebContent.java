package edu.uci.mhlee;
import java.util.List;

import org.apache.http.Header;

import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class WebContent {
	int docid;
	String url;
	String domain;
	String subDomain;
	String path;
	String parentUrl;
	String anchor;
	int textLength;
	int htmlLength;
	int wordcount;
	String text;
	String html;
	int outgoingLink;
	List<WebURL> links;
	String title;

	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		if(title != null)
			this.title = title.replace("'", "''");
		else
			this.title = "";
				
	}
	public List<WebURL> getLinks() {
		return links;
	}
	public void setLinks(List<WebURL> links) {
		this.links = links;
	}
	public int getWordcount() {
		return wordcount;
	}
	public void setWordcount(int wordcount) {
		this.wordcount = wordcount;
	}
	public int getDocid() {
		return docid;
	}
	public void setDocid(int docid) {
		this.docid = docid;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getSubDomain() {
		return subDomain;
	}
	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getParentUrl() {
		return parentUrl;
	}
	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}
	public String getAnchor() {
		return anchor;
	}
	public void setAnchor(String anchor) {
		if(anchor != null)
			this.anchor = anchor.replace("'", "''");
		else 
			this.anchor = "";
	}
	public int getTextLength() {
		return textLength;
	}
	public void setTextLength(int textLength) {
		this.textLength = textLength;
	}
	public int getHtmlLength() {
		return htmlLength;
	}
	public void setHtmlLength(int htmlLength) {
		this.htmlLength = htmlLength;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		if(text != null)
			this.text = text.replace("'", "''");
		else 
			this.text = "";
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		if(html != null)
			this.html = html.replace("'", "''");
		else 
			this.html = "";
	}
	public int getOutgoingLink() {
		return outgoingLink;
	}
	public void setOutgoingLink(int outgoingLink) {
		this.outgoingLink = outgoingLink;
	}
	@Override
	public String toString() {
		return "WebContent [docid=" + docid + ", url=" + url + ", domain=" + domain + ", subDomain=" + subDomain
				+ ", path=" + path + ", parentUrl=" + parentUrl + ", anchor=" + anchor + ", textLength=" + textLength
				+ ", htmlLength=" + htmlLength + ", wordcount=" + wordcount + ", text=" + text + ", html=" + html
				+ ", outgoingLink=" + outgoingLink + ", links=" + links + ", title=" + title + "]";
	}
}



