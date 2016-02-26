package edu.uci.mhlee;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

public class InvertedTable {
	static boolean isDebug = false;
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public static Map<String, Map<Integer, Integer>> computeInvertedIndex(List<String> stopWords){
		return computeInvertedIndex(1, stopWords);
	}
	
	public static Map<String, Map<Integer, Integer>> computeInvertedIndex(int nGram, List<String> stopWords){
		Map<String, Map<Integer, Integer>> invertedIndex = new TreeMap<String, Map<Integer, Integer>>();
		int maxRow = DBUtils.getTotalSize("webContents");
		int step = 10000;

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
//			Statement statement2 = connection.createStatement();
//			statement2.setQueryTimeout(30);  // set timeout to 30 sec.

			for(int i = 0 ; i < maxRow ; i+=step){
				String curQuery = "select docid, text from webContents limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);
				while(rs.next())
				{
					// read the result set
					int docid = rs.getInt("docid");
					String text = rs.getString("text");
					//String[] textParts = text.trim().toLowerCase().split("\\s");
					String[] textParts = Utils.mySplit(text);
					List<String> trimedList = new ArrayList<String>();
					for(int j = 0 ; j < textParts.length ; j++){
						String curStr = textParts[j].trim().replaceAll("^['0-9]+", "").replaceAll("['0-9]+$","").replaceAll("'", "''"); 
						if(curStr.length() > 1) trimedList.add(curStr);
					}

					//System.out.println(trimedList);

					for(int j = 0 ; j < trimedList.size()-nGram+1 ; j++){
						String token = "";
						boolean isValid = true;
						for(int k = j ; k < j+nGram ; k++){
							if(stopWords.contains(trimedList.get(k).toLowerCase())){
								isValid = false;
								break;
							}
							token += trimedList.get(k)+" ";
						}
						token = token.trim();

						if(!isValid) continue;
						
						if(invertedIndex.get(token) == null){
							invertedIndex.put(token, new HashMap<Integer, Integer>());
						}
						
						if(invertedIndex.get(token).get(docid) == null){
							invertedIndex.get(token).put(docid, 0);
						}
						
						int curFrequency = invertedIndex.get(token).get(docid);
						invertedIndex.get(token).put(docid, curFrequency+1);

					}
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

		return invertedIndex;
	}
	
	public static void pushInvertedIndex(Map<String, Map<Integer, Integer>>  invertedIndex, int nGram){
		// TODO Auto-generated method stub


		//Get rank query
		//select (select count(*) from wordFrequency b  where a.id >= b.id and a.ngram = b.ngram) as cnt, 
		//      word, frequency from wordFrequency a where ngram = 3 limit 20
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			
			statement.executeUpdate("create table invertedIndex (id integer, word string, docid integer, tf integer, ngram integer)");

			int max = -1;
			int cur = 0;
			for(String word : invertedIndex.keySet()){
				cur++;
				if(isDebug)
					System.out.println("<"+cur+", "+invertedIndex.keySet().size()+">");
				for(Integer docid : invertedIndex.get(word).keySet()){
					int frequency = invertedIndex.get(word).get(docid);
					String exeQuery = "insert into invertedIndex values(null, '"+ word +"', "+docid+", "+frequency+","+nGram+")";
//					if(isDebug)
//						System.out.println(exeQuery);
					statement.executeUpdate(exeQuery);
				}
				
				if(max > 0 && cur == max) break;				
			}
			
			//ResultSet rs = statement.executeQuery("select * from webContents");
			
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
	}

	public static void printInvertedIndex(Map<String, Map<Integer, Integer>> invertedIndex){
		for(String word : invertedIndex.keySet()){
			System.out.println(word);
			Map<Integer, Integer> docIdTF = invertedIndex.get(word);
			docIdTF = Utils.sortByValueInt(docIdTF);
			for(Integer docId : docIdTF.keySet()){
				
				System.out.println("\t"+"docId: "+docId+", TF: "+docIdTF.get(docId));
			}
			System.out.println("");
		}
	}
	
	

	public static TreeMap<String, Map<Integer, Double>> computeTfIdf()
	{
		// dfMap has term-df
		TreeMap<String, Integer> dfMap = new TreeMap<String, Integer>();
		// tfdifMap has term-docid-tfidf
		TreeMap<String, Map<Integer, Double>> tfidfMap = new TreeMap<String, Map<Integer, Double>>();
		
		int step = 10000;
		int step2 = step * 100;
		String term, termForDf;
		int tf, df, docId;
		double tfidf;
		int numDocs = 147804;

		
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			
			int maxRow = DBUtils.getTotalSize("wordFrequency");
			
			// First load DFs into dfMap
			for (int i=0; i< maxRow; i+=step)
			{
				String curQuery = "select word, df from wordFrequency limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);
				
				while (rs.next())
				{
					// read the result set
					termForDf = rs.getString("word");
					df = rs.getInt("df");
					dfMap.put(termForDf, df);
				}
			}
			
			System.out.println("--------------------------------------");

			maxRow = DBUtils.getTotalSize("invertedIndex");
			// Now load TFs and calculate tfidf for each (term-docid) pair
			for (int i=0 ; i< maxRow ; i+=step2)
			{
				String curQuery = "select word, docid, tf from invertedIndex limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);
				while (rs.next())
				{
					// read the result set
					term = rs.getString("word");
					docId = rs.getInt("docid");
					tf = rs.getInt("tf");
					
					if (dfMap.get(term) == null)
					{
						System.out.println("Word "+term+" not in dfMap!");
						continue;
					}
					else
						df = dfMap.get(term);
					
					tfidf = ( 1 + Math.log(tf) ) * Math.log( (double)numDocs / df );
				
					if (tfidfMap.get(term) == null)
						tfidfMap.put(term, new HashMap<Integer, Double>());
					
					if (tfidfMap.get(term).get(docId) == null)
					{
						System.out.println(term + ", " + docId + ": " + tfidf);
						tfidfMap.get(term).put(docId, tfidf);
					}
					else
						System.out.println("Duplicate term-docid pair? : " + term + "-" + docId);
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
		return tfidfMap;
	}
	

	
	public static void pushTfIdf (TreeMap<String, Map<Integer, Double>> tfidfMap)
	{
		//Get rank query
		//select (select count(*) from wordFrequency b  where a.id >= b.id and a.ngram = b.ngram) as cnt, 
		//      word, frequency from wordFrequency a where ngram = 3 limit 20
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();			
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			statement.executeUpdate("alter table invertedIndex add column tfidf double"); // Do this just once
			
			int max = -1;
			int cur = 0;
			for(String word : tfidfMap.keySet())
			{					
				for(Integer docid : tfidfMap.get(word).keySet()){
					cur++;
					double tfidf = tfidfMap.get(word).get(docid);
//					String exeQuery = "insert into invertedIndex values(null, '"+ word +"', "+docid+", "+frequency+","+nGram+")";
					String exeQuery = "update invertedIndex set tfidf=" + tfidf + " where word='" + word + "' and docid=" + docid;
					System.out.println(exeQuery);

					statement.executeUpdate(exeQuery);
					
				}
				
				if(max > 0 && cur == max) break;	
							
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

	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		List<String> stopWords = Utils.readStopWords("stopwords.txt");
		long startTime = System.currentTimeMillis();
		long endTime1, endTime3;
		long endTime2 = startTime;
		
		for(int nGram = 1 ; nGram <= 1 ; nGram++)
		{
			Map<String, Map<Integer, Integer>> invertedIndex = computeInvertedIndex(nGram, stopWords);
			endTime1 = System.currentTimeMillis();
			System.out.println("now pushing");
			pushInvertedIndex(invertedIndex, nGram);
			endTime2 = System.currentTimeMillis();
			long lTime_for_read = endTime1 - startTime;
		    long lTime_for_invertedIndex = endTime2 - startTime;
			System.out.println("TIME (read) : " + lTime_for_read + "(ms)");
		    System.out.println("TIME (invertedIndex) : " + lTime_for_invertedIndex + "(ms)");
		}
	
		TreeMap<String, Map<Integer, Double>> tfidfMap = computeTfIdf();
		System.out.println("Now updating with tf-idf");
		pushTfIdf(tfidfMap);
		endTime3 = System.currentTimeMillis();
	
	    long lTime_for_tfidf = endTime3 - endTime2;
	    long lTime_for_all = endTime3 = startTime;
	    
	    System.out.println("TIME (tfidf) : " + lTime_for_tfidf + "(ms)");
	    System.out.println("TIME (all) : " + lTime_for_all + "(ms)");	
	
		
	}
	
	

}
