package edu.uci.mhlee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRank {
	public static int DOCID_NOT_FOUND = -1;
	static boolean isDebug = true;
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public static Map<String, Integer> readUrl2DocidMap(){
		Map<String, Integer> url2DocidMap = new HashMap<String, Integer>();
		
		int docid;
		String url;
		
		int step = 10000;
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			int maxRow = DBUtils.getTotalSize("webContents");

			// First load DFs into dfMap
			for (int i=0; i< maxRow; i+=step)
			{
				String curQuery = "select docid, url from webContents limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);

				while (rs.next())
				{
					// read the result set
					docid = rs.getInt("docid");
					url = rs.getString("url");
					
					url2DocidMap.put(url, docid);
					
					//dfMap.put(word, df);
				}
			}

			System.out.println("--------------------------------------");


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
		
		return url2DocidMap;
	}
	
	/**
	 * Convert String type url to docid.
	 * @param webLinkOut
	 * @param url2doc
	 * @return
	 */
	public static Map<Integer, List<Integer>> convertUrl2Docid(Map<Integer, List<String>> webLinkOut, Map<String, Integer> url2doc){
		Map<Integer, Map<Integer, Integer>> webLinkOutIntMap = new HashMap<Integer, Map<Integer, Integer>>();
		Map<Integer, List<Integer>> webLinkOutInt = new HashMap<Integer, List<Integer>>();
		
		for(Integer docid : webLinkOut.keySet()){
			if(webLinkOutIntMap.get(docid) == null) webLinkOutIntMap.put(docid, new HashMap<Integer, Integer>());
			
			List<String> outLinks = webLinkOut.get(docid);
			for(String url : outLinks){
				Integer docid2 = url2doc.get(url);
				if(docid2 == null) docid2 = DOCID_NOT_FOUND;
				webLinkOutIntMap.get(docid).put(docid2, 1);	
			}
		}
		
		
		for(Integer docid : webLinkOutIntMap.keySet()){
			if(webLinkOutInt.get(docid) == null) webLinkOutInt.put(docid, new ArrayList<Integer>());
			Map<Integer, Integer> linkMap = webLinkOutIntMap.get(docid);
			for(Integer docid2 : linkMap.keySet()){
				webLinkOutInt.get(docid).add(docid2);
			}
		}
		
		return webLinkOutInt;
	}
	
	/**
	 * Construct ingoing map from outgoing map
	 * @param webLinkOut
	 * @param url2doc
	 * @return
	 */
	public static Map<Integer, List<Integer>> getLinkIn(Map<Integer, List<Integer>> webLinkOut){
		Map<Integer, List<Integer>> webLinkIn = new HashMap<Integer, List<Integer>>();
		
		for(Integer docid : webLinkOut.keySet()){
			
			List<Integer> outLinks = webLinkOut.get(docid);
			for(Integer outDocid : outLinks){
				if(webLinkIn.get(outDocid) == null) webLinkIn.put(outDocid, new ArrayList<Integer>());
				if(!webLinkIn.get(outDocid).contains(docid)) webLinkIn.get(outDocid).add(docid);
			}
		}
		
		return webLinkIn;
	}
	
	public static Map<Integer, List<String>> readWebLinks(){
		Map<Integer, List<String>> webLinks = new HashMap<Integer, List<String>>();
		
		int docid;
		String url;
		
		int step = 100000;
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			int maxRow = DBUtils.getTotalSize("webLinks");

			// First load DFs into dfMap
			for (int i=0; i< maxRow; i+=step)
			{
				String curQuery = "select docid, url from webLinks limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);

				while (rs.next())
				{
					// read the result set
					docid = rs.getInt("docid");
					url = rs.getString("url");
					
					if(webLinks.get(docid) == null) webLinks.put(docid, new ArrayList<String>());
					webLinks.get(docid).add(url);
					
					//dfMap.put(word, df);
				}
			}

			System.out.println("--------------------------------------");


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
		
		
		return webLinks;
	}
	
	public static Map<Integer, List<Integer>> readWebLinksIn(){
		Map<Integer, List<Integer>> webLinksIn = new HashMap<Integer, List<Integer>>();
		
		int docid;
		int indocid;
		
		int step = 100000;
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			int maxRow = DBUtils.getTotalSize("webLinks");

			// First load DFs into dfMap
			for (int i=0; i< maxRow; i+=step)
			{
				String curQuery = "select docid, indocid from webLinksIn limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);

				while (rs.next())
				{
					// read the result set
					docid = rs.getInt("docid");
					indocid = rs.getInt("indocid");
					
					if(webLinksIn.get(docid) == null) webLinksIn.put(docid, new ArrayList<Integer>());
					webLinksIn.get(docid).add(indocid);
					
					//dfMap.put(word, df);
				}
			}

			System.out.println("--------------------------------------");


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
		
		
		return webLinksIn;
	}
	
	public static void pushInLinkTable(Map<Integer, List<Integer>> webLinksIn){
		Connection connection = null;
		String sql = "";
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			int max = -1;
			int cur = 0;
			for(Integer docid : webLinksIn.keySet()){
				cur++;
				List<Integer> inLinks = webLinksIn.get(docid);
				for(Integer inLink : inLinks){
					sql = "insert into webLinksIn values(null, "+docid+", "+inLink+")";
					statement.executeUpdate(sql);
				}				
				if(max > 0 && cur == max) break;
			}		
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage()+", "+sql);
		}
		finally
		{
			try
			{
				if(connection != null){
					connection.setAutoCommit(true);
					connection.close();
				}
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}
	}
	
	
	public static Map<Integer, Double> computePageRank(Map<Integer, List<Integer>> webLinksIn, Map<Integer, List<Integer>> webLinksOut, int maxCount, Map<Integer, Double> pRank, int type){
		if(pRank == null){
			pRank = new HashMap<Integer, Double>();
			
			for(Integer docid : webLinksIn.keySet())
				pRank.put(docid, 1.0);
			
		}
		
		//int type = 1;
		
		double d = 0.85;
		double e = 0.01;
		boolean isDeadend = false;
		int next = 1;//(int) (Math.floor(Math.random()*pRank.keySet().size()));
		double diffSum = 0.0;
		if(type == 1){
			List<Integer> prevOutgoing = null;
			for(int i = 0 ; i < maxCount ; i++){
				if(i % 10000 == 0)
					System.out.println(i+"/"+maxCount);
				
				if(isDeadend){
					next = (int) (Math.floor(Math.random()*pRank.keySet().size()));
					isDeadend = false;
				}else{
					if(prevOutgoing != null){
						//Choose one of outlink
						
						int curNext = next;
						//Make sure next is different to current
						int trialCnt = 0;
						int trial = 100;
						while(curNext == next || curNext == DOCID_NOT_FOUND){
							int randOutIdx = (int)Math.floor(Math.random() * prevOutgoing.size());
							curNext = prevOutgoing.get(randOutIdx);
							trialCnt++;
							if(trialCnt > trial) break;
						}
						
						//Couldn't find proper link
						if(trialCnt > trial){
							//Then do random.
							next = (int) (Math.floor(Math.random()*pRank.keySet().size()));
						}else{
							next = curNext; 
						}
						
						prevOutgoing = null;
					}
						
				}
				
				double pVal = 0;
				
				//System.out.println("next = "+next);
				List<Integer> linkIn = webLinksIn.get(next);
				if(linkIn == null){
					isDeadend = true;
					continue;
					
				}
				
				for(Integer inDoc : linkIn){
					Double curPVal = pRank.get(inDoc);
					List<Integer> curOutgoing = webLinksOut.get(inDoc);
					double curOutgoingSize = e;
					if(curOutgoing != null)
						curOutgoingSize += curOutgoing.size();
					if(curPVal == null) continue;
					
					pVal += curPVal / curOutgoingSize;
				}
				
				pVal = (1-d) + d*pVal;
				pRank.put(next, pVal);
				prevOutgoing = webLinksOut.get(next);
				//System.out.println("prevOutgoing :"+prevOutgoing);
				if(prevOutgoing == null || prevOutgoing.size() == 0)
					isDeadend = true;
				
			}
		}else if(type == 2){
			//int totalSize = pRank.keySet().size();
			int cnt = 0;
			for(Integer i : pRank.keySet()){
				cnt++;
			//for(int i = 1 ; i <= totalSize ; i++){
				if(cnt % 10000 == 0)
					System.out.println(cnt+"/"+pRank.keySet().size());
				next = i;
				
				
				
				double pVal = 0;
				
				//System.out.println("next = "+next);
				List<Integer> linkIn = webLinksIn.get(next);
				if(linkIn == null){
					System.out.println(next+" is null");
					break;
					
				}
				
				for(Integer inDoc : linkIn){
					Double curPVal = pRank.get(inDoc);
					List<Integer> curOutgoing = webLinksOut.get(inDoc);
					double curOutgoingSize = e;
					if(curOutgoing != null)
						curOutgoingSize += curOutgoing.size();
					if(curPVal == null) continue;
					
					pVal += curPVal / curOutgoingSize;
				}
				
				pVal = (1-d) + d*pVal;
				double prevPRank = pRank.get(next);
				pRank.put(next, pVal);
				diffSum += Math.abs(pVal - prevPRank);
			}
		}
		System.out.println("Total Changed :"+diffSum);
		return pRank;
	}
	
	public static Map<Integer, Double> readPageRank(){
		Map<Integer, Double> pRank = new HashMap<Integer, Double>();
		
		int docid;
		double p;
		
		int step = 100000;
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			int maxRow = DBUtils.getTotalSize("pageRank");

			// First load DFs into dfMap
			for (int i=0; i< maxRow; i+=step)
			{
				String curQuery = "select docid, prank from pageRank limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);

				while (rs.next())
				{
					// read the result set
					docid = rs.getInt("docid");
					p = rs.getDouble("prank");
					
					pRank.put(docid, p);
					
					//dfMap.put(word, df);
				}
			}

			System.out.println("--------------------------------------");


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
				if(connection != null){
					connection.setAutoCommit(true);
					connection.close();
				}
					
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}
		
		
		return pRank;
	}
	public static void pushPageRank(Map<Integer, Double> pRank){
		Connection connection = null;
		String sql = "";
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			sql = "delete from pageRank where id > 0";
			statement.executeUpdate(sql);
			
			int max = -1;
			int cur = 0;
			for(Integer docid : pRank.keySet()){
				cur++;
				Double p = pRank.get(docid);
				
				sql = "insert into pageRank values(null, "+docid+", "+p+")";
				statement.executeUpdate(sql);
								
				if(max > 0 && cur == max) break;
			}		
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage()+", "+sql);
		}
		finally
		{
			try
			{
				if(connection != null){
					connection.setAutoCommit(true);
					connection.close();
				}
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}
	}
	
	
	public static void main(String[] args){
		Map<String, Integer> url2docid = readUrl2DocidMap();
		Map<Integer, List<String>> webLinksOut = readWebLinks();
		Map<Integer, List<Integer>> webLinksOutInt = convertUrl2Docid(webLinksOut, url2docid);
//		Map<Integer, List<Integer>> webLinksIn = getLinkIn(webLinksOutInt);
//		pushInLinkTable(webLinksIn);
		
		Map<Integer, List<Integer>> webLinksIn = readWebLinksIn();
//		System.out.println(webLinksIn.get(123615));
		
		
//		int ii = 0 ;
//		for( Integer docid : webLinksIn.keySet()){
//			System.out.println(docid+"/"+webLinksIn.get(docid));
//			ii++;
//			if(ii > 10) break;
//		}
//		
//		if(1==1) return;
		//System.out.println(webLinksIn);
		int computeCount = 300000;
		int maxIter = 10;
		
		Map<Integer, Double> pRank = readPageRank();//computePageRank(webLinksIn, webLinksOutInt, computeCount, null);
		
		for(int i = 0 ; i < maxIter ; i++){
			System.out.println(i+"/'"+maxIter);
			pRank = computePageRank(webLinksIn, webLinksOutInt, computeCount, pRank, 1);
			System.out.println("Push...");
			pushPageRank(pRank);
			System.out.println("End...");
		}
		
		
	}
	
	
	
	
}

