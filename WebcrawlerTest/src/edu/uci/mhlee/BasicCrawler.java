package edu.uci.mhlee;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.Header;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Yasser Ganjisaffar [lastname at gmail dot com]
 */
public class BasicCrawler extends WebCrawler {
	static Connection connection = null;
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			
		} catch(Exception e) { e.printStackTrace(); }
	}

	private final static Pattern BINARY_FILES_EXTENSIONS =
			Pattern.compile(".*\\.(bmp|gif|jpe?g|png|tiff?|pdf|ico|xaml|pict|rif|pptx?|ps" +
					"|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox" +
					"|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv" +
					"|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha)" +
					"(\\?.*)?$"); // For url Query parts ( URL?q=... )

	
	
	/**
	 * You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		//logger.info(url+" is... "+(!BINARY_FILES_EXTENSIONS.matcher(href).matches() && href.contains("ics.uci.edu")));
		return 
				!BINARY_FILES_EXTENSIONS.matcher(href).matches()	//Exclude media or other invalid extensions 
				&& href.contains("ics.uci.edu") 					//Include only ics.uci.edu
				&& !isAlreadyVisited(url)							//Exclude already visited URL
				&& !(href.contains("?") && href.contains("="));		//exclude url query
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		int docid = page.getWebURL().getDocid();
		String url = page.getWebURL().getURL();
		String domain = page.getWebURL().getDomain();
		String path = page.getWebURL().getPath();
		String subDomain = page.getWebURL().getSubDomain();
		String parentUrl = page.getWebURL().getParentUrl();
		String anchor = page.getWebURL().getAnchor();

		WebContent webContent = new WebContent();
		logger.debug("Docid: "+ docid);
		logger.info("URL: "+ url);
		logger.debug("Domain: "+ domain);
		logger.debug("Sub-domain: "+ subDomain);
		logger.debug("Path: "+ path);
		logger.debug("Parent page: "+ parentUrl);
		logger.debug("Anchor text: "+ anchor);
		webContent.setDocid(docid);
		webContent.setUrl(url);
		webContent.setDomain(domain);
		webContent.setSubDomain(subDomain);
		webContent.setPath(path);
		webContent.setParentUrl(parentUrl);
		webContent.setAnchor(anchor);


		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			String title = htmlParseData.getTitle();
			List<WebURL> links = htmlParseData.getOutgoingUrls();

			logger.debug("Text length: "+ text.length());
			logger.debug("Html length: "+ html.length());
			logger.debug("Number of outgoing links: "+ links.size());

			webContent.setTextLength(text.length());
			webContent.setHtmlLength(html.length());
			
			webContent.setText(text);
			webContent.setHtml(html);
			webContent.setTitle(title);
			
			String[] stParts = Utils.mySplit(text);
			webContent.setWordcount(stParts.length);
			webContent.setOutgoingLink(links.size());
			webContent.setLinks(links);

		}

		Header[] responseHeaders = page.getFetchResponseHeaders();
		if (responseHeaders != null) {
			logger.debug("Response headers:");
			for (Header header : responseHeaders) {
				logger.debug(header.getName()+": "+ header.getValue());
			}
		}

		logger.debug("=============");
		write2DB(webContent);
	}

	public int write2DB(WebContent content){
		logger.info("docid:"+content.getDocid()+" added.");
		
		String insertSQL = null;
		try
		{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			insertSQL = "insert into webContents values("+
					"null,"+ //auto increment
					content.getDocid()+","+
					"'"+content.getUrl()+"',"+
					"'"+content.getSubDomain()+"',"+
					"'"+content.getPath()+"',"+
					"'"+content.getParentUrl()+"',"+
					"'"+content.getAnchor()+"',"+
					""+content.getTextLength()+","+
					""+content.getHtmlLength()+","+
					""+content.getWordcount()+","+
					"'"+content.getText()+"',"+
					"'"+content.getHtml()+"',"+
					""+content.getOutgoingLink()+","+
					"'"+content.getTitle()+"'"+
					")";
			

			statement.executeUpdate(insertSQL);
			
			for(WebURL url : content.getLinks()){
				insertSQL = "insert into webLinks values("+
						"null,"+
						content.getDocid()+","+
						"'"+url.getURL()+"'"+
						")";
				statement.executeUpdate(insertSQL);
			}

		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
			System.err.println("========================");
			System.err.println(insertSQL);
			System.err.println("========================");
		}
		finally
		{
//			try
//			{
//				if(connection != null)
//					connection.close();
//			}
//			catch(SQLException e)
//			{
//				// connection close failed.
//				System.err.println(e);
//			}
		}

		return 0;
	}

	public boolean isAlreadyVisited(WebURL url){
		boolean isAlreadyVisited = false;
		try
		{
			// create a database connection
			
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			String urlString = url.getURL().toLowerCase();
			//url.get
			
			ResultSet rs = statement.executeQuery("select count(*) as cnt from webContents where url = '"+urlString+"'");
			while(rs.next())
			{
				// read the result set
				//System.out.println("cnt = " + rs.getString("name"));
				//System.out.println("id = " + rs.getInt("id"));
				if(Integer.parseInt(rs.getString("cnt"))>0){
					isAlreadyVisited = true;
				}
			}
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		finally
		{
//			try
//			{
//				if(connection != null)
//					connection.close();
//			}
//			catch(SQLException e)
//			{
//				// connection close failed.
//				System.err.println(e);
//			}
		}
		
		return isAlreadyVisited;
	}
	
	
	
	public static void main(String[] args){
		String href = "http://aaaa/?alskdjfalk=10";
		String[] aaa = href.split("(\\?.*)?$");
		System.out.println(aaa[0]);
		System.out.println(aaa.length);
		Pattern URL_QUERY = Pattern.compile(".*\\.(\\?.*)?$");
		System.out.println(URL_QUERY.matcher(href).matches());
		System.out.println(href.contains("?"));
	}
}