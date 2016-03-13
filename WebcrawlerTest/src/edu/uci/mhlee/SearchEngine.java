package edu.uci.mhlee;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import edu.uci.ics.crawler4j.url.WebURL;

public class SearchEngine {
	static boolean isDebug = true;
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}

	public static void main(String[] args) {	
		String query = "Minhaeng";
		int rankType = 2;
//		double anchorWeight = 50;
//		double titleWeight = 100000;
//		double pageRankWeight = 1;
//		double pageRankMax = 9999;
//		double pageRankInit = 0.5;
		
		double anchorWeight = 100;
		double titleWeight = 100;
		double pageRankWeight = 1;
		double pageRankMax = 9999;
		double pageRankInit = 0.5;

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			Map<Integer, Double> pRank = PageRank.readPageRank();
			
			Scanner keyboard = new Scanner(System.in);
			
			
			while(!"exit".equals(query.toLowerCase())){//query.length() >= 0){
				Map<Integer, Double> docScoreMap = new HashMap<Integer, Double>();
				List<Map<Integer, String>> posMapList = new ArrayList<Map<Integer, String>>();
				Map<Integer, String> posMap = null;//new HashMap<Integer, String>();
//				HashSet<Integer> docSet = new HashSet<Integer>();
				
				System.out.print("Plase enter query: ");
				query = keyboard.nextLine();
				String[] queryParts = query.split(" ");

				//String word;
				int tf, df, docid;
				double tfidf;
				String pos;

				int queryCnt = 0;
				for(String queryWord : queryParts){
					Map<Integer, String> curPosMap = new HashMap<Integer, String>();
					
					
					String extra = "";
					if (queryWord.matches(".*s$"))
						extra = " or word = '" + queryWord.replaceAll("s$", "") + "'";
						
					
					// First load DFs into dfMap

					{
						//Based on text content
						String curQuery = "select docid, tf, df, tfidf, pos from invertedIndex where word = '"+queryWord+"'"+ " or word = '" + capitalize(queryWord) + "'" + extra;
						//System.out.println(curQuery);
						ResultSet rs = statement.executeQuery(curQuery);
	
						while (rs.next())
						{
							// read the result set
							//word = rs.getString("word");
							docid = rs.getInt("docid");
							tf = rs.getInt("tf");
							df = rs.getInt("df");
							tfidf = rs.getDouble("tfidf");
							pos = rs.getString("pos");
	
							// For Cosine Similarity
							// Get the doc ids that have the query word and add them to the set
//							docSet.add(docid);
	
							if(docScoreMap.get(docid) == null) docScoreMap.put(docid, 0.0);
							//Individual adding
							if(rankType == 1){
								docScoreMap.put(docid, docScoreMap.get(docid)+tfidf);	
							}else if(rankType == 2){
								//Just copy only for the first query word
								if(queryCnt==0){
									curPosMap.put(docid, pos);
									docScoreMap.put(docid, docScoreMap.get(docid)+tfidf);	
								}
								//Need to check from second query word
								else{
									//Co-occurrence
									if(posMap.get(docid) != null){
										String matchedPos = Utils.neighborPosString(posMap.get(docid), pos, 1);
										curPosMap.put(docid, matchedPos);
										int matchedPosCnt = 0;
										if(matchedPos.length() > 0)
											matchedPosCnt = matchedPos.split(",").length;
	
						
										docScoreMap.put(docid, docScoreMap.get(docid)+tfidf*(Math.log(1+matchedPosCnt)+1));
	
										//log(1+matchedPosCnt)+1
									}
								}
							}
	
						}
	
						posMap = curPosMap;
						posMapList.add(posMap);
					}
					
					
					{
						//Add Anchor weight
						String curQuery = "select docid, df from invertedIndexAnchor where type = 'anchor' and word = '"+queryWord+"'"+ " or word = '" + capitalize(queryWord) + "'" + extra;
						//System.out.println(curQuery);
						ResultSet rs = statement.executeQuery(curQuery);
						while (rs.next())
						{
							// read the result set
							//word = rs.getString("word");
							docid = rs.getInt("docid");
							df = rs.getInt("df");
							
							if(docScoreMap.get(docid) == null) docScoreMap.put(docid, 0.0);
							double curScore = docScoreMap.get(docid);
//							docScoreMap.put(docid, curScore+anchorWeight/df);
							docScoreMap.put(docid, curScore+anchorWeight);
						}
					}
					
					{
						//Add Title weight
						String curQuery = "select docid, df from invertedIndexAnchor where type = 'title' and word = '"+queryWord+"'"+ " or word = '" + capitalize(queryWord) + "'" + extra;
						//System.out.println(curQuery);
						ResultSet rs = statement.executeQuery(curQuery);
						while (rs.next())
						{
							// read the result set
							//word = rs.getString("word");
							docid = rs.getInt("docid");
							df = rs.getInt("df");
							
							if(docScoreMap.get(docid) == null) docScoreMap.put(docid, 0.0);
							double curScore = docScoreMap.get(docid);
//							docScoreMap.put(docid, curScore+titleWeight/df);
							docScoreMap.put(docid, curScore+titleWeight);
						}
					}
					
					queryCnt++;
				}
				
				
				// Add pageRank Concept
				for(Integer curDocid : docScoreMap.keySet()){
					Double curScore = docScoreMap.get(curDocid);
					Double pVal = pRank.get(curDocid);
					if(pVal == null) pVal = pageRankInit;
					
					docScoreMap.put(curDocid, curScore + pageRankWeight*Math.min(pVal, pageRankMax));
				}
				
				
			
				docScoreMap = Utils.sortByValueDouble(docScoreMap);
				
				
				// Print 
				print(docScoreMap);
			

				// Calculate NDCG Score
				{
					// Get GoogleList
					ArrayList<String> googleList = getTopURLsFromGoogle(query);
					
					// Get GobuciList
					ArrayList<String> gobuciList = new ArrayList<String>();
					for(Integer key : docScoreMap.keySet())
					{
						String curQuery = "select url from webContents where docid = "+key;
						ResultSet rs = statement.executeQuery(curQuery);
						String url;
						int cnt = 0;
						while (rs.next())
						{
							url = rs.getString("url");
							gobuciList.add(url);
							if (cnt == 5) break;
						}
					}
					
					double NDCG5 = computeNDCG5(googleList, gobuciList);
					// Print NDCG@5 Score
					System.out.println("\nNDCG@5: "+NDCG5);
				}
					
			}

			System.out.println("\n");
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		finally
		{
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}

	}


	
	private static String capitalize(final String line) {
		   return Character.toUpperCase(line.charAt(0)) + line.substring(1);
		}
	
	/**
	 * Relevance scores have the scale 1 to 5.
	 * @param googleList
	 * @param gobuciList
	 * @return
	 */
	public static double computeNDCG5(ArrayList<String> googleList, ArrayList<String> gobuciList)
	{
		
		double IDCG = 12.3234658; // Ideal value. When perfectly aligned.
		if (googleList.size() == 4)
			IDCG = 11.892789;
		else if (googleList.size() == 3)
			IDCG = 10.892789;
		else if (googleList.size() == 2)
			IDCG = 9.0;
		
		double[] DC = new double[5];
			
		double DCG = 0.0;
		double rel;
		
		for (int i = 0; i < Math.min(5, gobuciList.size()); i++) {
			for (int j = 0; j < googleList.size(); j++) {
				String pStr = gobuciList.get(i).substring(gobuciList.get(i).indexOf("/"));
				String gStr = googleList.get(j).substring(googleList.get(j).indexOf("/"));
				
				if(pStr.substring(pStr.lastIndexOf("/")).contains("index"))
					pStr = pStr.substring(pStr.indexOf("/"), pStr.lastIndexOf("/"));
				else if(pStr.substring(pStr.length()-1).equals("/"))
					pStr = pStr.substring(pStr.indexOf("/"), pStr.length()-1);
				
				if(gStr.substring(gStr.lastIndexOf("/")).contains("index"))
					gStr = gStr.substring(gStr.indexOf("/"), gStr.lastIndexOf("/"));
				else if(gStr.substring(gStr.length()-1).equals("/"))
					gStr = gStr.substring(gStr.indexOf("/"), gStr.length()-1);
				
				
				// If the two URLs match -> Add DCG value
				if( pStr.equals(gStr) ) 
				{
//					System.out.println("i:"+i+", j:"+j);
					double denom = log2(i+1);
					if(denom == 0)
						denom = 1;
					rel = 5-j;
					DC[i] = rel/denom;
					DCG += DC[i];
					break;
				}
			}
		}
		
		double NDCG = (DCG / IDCG);
		return NDCG;
	}
	
	public static double log2(double d) {
		return (double) (Math.log(d) / Math.log(2.0));
	}
	

	/**
	 * @param query
	 * @return ArrayList<String> : List of top urls from google
	 */
	public static ArrayList<String> getTopURLsFromGoogle(String query)
	{
		ArrayList<String> googleList = new ArrayList<String>();
		
		String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&start=0&rsz=8&q=";
	    String search = "site:ics.uci.edu";
	    String charset = "UTF-8";
	    
	    // We are going to consider the results without the following extensions.
	    final Pattern BINARY_FILES_EXTENSIONS =
				Pattern.compile(".*\\.(bmp|gif|jpe?g|png|tiff?|pdf|ico|xaml|pict|rif|pptx?|ps" +
						"|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox" +
						"|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv" +
						"|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha)" +
						"(\\?.*)?$"); // For url Query parts ( URL?q=... )
	    try
	    {
	    	String strUrl;
	    	URL url = new URL(google + URLEncoder.encode(search+" "+query, charset));
		    Reader reader = new InputStreamReader(url.openStream(), charset);
		    GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);
		    int size = results.getResponseData().getResults().size();
		    for(int i = 0; i < size; i++)
		    {
		    	strUrl = results.getResponseData().getResults().get(i).getUrl();
		    	if (!BINARY_FILES_EXTENSIONS.matcher(strUrl).matches())
		    		googleList.add(strUrl);
	    		if (googleList.size() == 5) break;
		    }
		    	
//		    System.out.println(googleList);
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    
	    return googleList;
	}

	
	

	/**
	 * Print document information from input map
	 * @param wordFrequencies
	 */
	public static void print(Map<Integer, Double> wordFrequencies){

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			//while(query.length() >= 0){
			//query = keyboard.nextLine();

			int docid;
			String url;
			String subdomain;
			String path;
			String title;
			int j = 0;
			NumberFormat formatter = new DecimalFormat("#0.000");     
			for(Integer key : wordFrequencies.keySet()){
				System.out.println(key+"/"+wordFrequencies.get(key));

				String curQuery = "select docid, url, subdomain, path, title from webContents where docid = "+key;
				//System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);

				while (rs.next())
				{
					// read the result set
					docid = rs.getInt("docid");
					url = rs.getString("url");
					subdomain = rs.getString("subdomain");
					path = rs.getString("path");
					title = rs.getString("title");

					System.out.println("Rank<"+(j+1)+"> Score:"+formatter.format(wordFrequencies.get(key))+"\tDocID: "+docid+"\tTitle: "+title+"\tURL: "+url+"\tSubDomain: "+subdomain+"\tPath:"+path);
					
				}
			
				j++;
				if(j > 9) break;
			}
			
			System.out.println("");
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		finally
		{
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}


	}


}
