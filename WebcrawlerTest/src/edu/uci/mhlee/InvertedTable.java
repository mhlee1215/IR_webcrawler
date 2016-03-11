package edu.uci.mhlee;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import edu.uci.mhlee.domains.InvertedIndexData;

import java.util.List;
import java.util.Map;

public class InvertedTable {
	static boolean isDebug = true;
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}

	public static InvertedIndexData computeInvertedIndex(List<String> stopWords){
		return computeInvertedIndex(1, stopWords, null);
	}

	public static InvertedIndexData computeInvertedIndex(int ngram, List<String> stopWords){
		return computeInvertedIndex(ngram, stopWords, null);
	}

	public static InvertedIndexData computeInvertedIndex(List<String> stopWords, Map<String, Integer> df){
		return computeInvertedIndex(1, stopWords, df);
	}

	 
	
	public static InvertedIndexData computeInvertedIndex(int nGram, List<String> stopWords, Map<String, Integer> dfMap){
		InvertedIndexData idx = new InvertedIndexData();
		Map<String, Map<Integer, Double>> tfidf = new TreeMap<String, Map<Integer, Double>>();
		Map<String, Map<Integer, Integer>> tf = new TreeMap<String, Map<Integer, Integer>>();
		Map<String, Map<Integer, Integer>> df = new TreeMap<String, Map<Integer, Integer>>();
		Map<String, Map<Integer, String>> wordPos = new TreeMap<String, Map<Integer, String>>();
		int maxRow = DBUtils.getTotalSize("webContents");
		int N = maxRow;
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
					String[] textParts = Utils.mySplit(text);
					List<String> trimedList = new ArrayList<String>();
					for(int j = 0 ; j < textParts.length ; j++){
						String curStr = Utils.myTrimmer(textParts[j]);//textParts[j].trim().replaceAll("^['0-9]+", "").replaceAll("['0-9]+$","");//.replaceAll("'", "''"); 
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

						//Add if not exist
						if(tf.get(token) == null){
							tf.put(token, new HashMap<Integer, Integer>());
						}

						//Init
						if(tf.get(token).get(docid) == null){
							tf.get(token).put(docid, 0);
						}

						//Increament
						int curFrequency = tf.get(token).get(docid);
						tf.get(token).put(docid, curFrequency+1);
						
						//Add if not exist
						if(df.get(token) == null){
							df.put(token, new HashMap<Integer, Integer>());
						}

						//Init
						if(df.get(token).get(docid) == null){
							df.get(token).put(docid, dfMap.get(token));
						}


						if(wordPos.get(token) == null){
							wordPos.put(token, new HashMap<Integer, String>());
						}
						if(wordPos.get(token).get(docid) == null){
							wordPos.get(token).put(docid, "");
						}
						
						
						String curPosStr = wordPos.get(token).get(docid);
						if(curPosStr.length() == 0) wordPos.get(token).put(docid, Integer.toString(j));
						else wordPos.get(token).put(docid, curPosStr+","+Integer.toString(j));

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

		if(dfMap != null){
			for(String word : tf.keySet()){
				Map<Integer, Integer> docAndTF = tf.get(word);
				tfidf.put(word, new HashMap<Integer, Double>());
				for(Integer docid : docAndTF.keySet()){
					Integer iTF = docAndTF.get(docid);
					if(dfMap.get(word) == null){
						System.err.println(word);
					}
					double dTFIDF = Utils.computeTFIDF(iTF, N, dfMap.get(word));
					tfidf.get(word).put(docid, dTFIDF);
				}
			}
		}


		idx.setTFIDF(tfidf);
		idx.setTF(tf);
		idx.setDF(df);
		idx.setPos(wordPos);
		return idx;
	}
	
	
	public static InvertedIndexData computeInvertedIndexAnchor(){
		InvertedIndexData idx = new InvertedIndexData();
				
		Map<String, List<Integer>> anchorMap = new TreeMap<String, List<Integer>>();
		Map<String, List<Integer>> titleMap = new TreeMap<String, List<Integer>>();
		
		int maxRow = DBUtils.getTotalSize("webContents");
		int N = maxRow;
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
				String curQuery = "select docid, anchor, title from webContents limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);
				while(rs.next())
				{
					// read the result set
					int docid = rs.getInt("docid");
					{
						String anchor = rs.getString("anchor");
						String[] textParts = Utils.mySplit(anchor);
						
						List<String> trimedList = new ArrayList<String>();
						for(int j = 0 ; j < textParts.length ; j++){
							String curStr = Utils.myTrimmer(textParts[j]);//textParts[j].trim().replaceAll("^['0-9]+", "").replaceAll("['0-9]+$","");//.replaceAll("'", "''"); 
							if(curStr.length() > 1) trimedList.add(curStr);
						}
	
						//System.out.println(trimedList);
	
						for(int j = 0 ; j < trimedList.size() ; j++){
							String token = trimedList.get(j);
							token = token.trim();
	
							//Add
							if(anchorMap.get(token) == null) anchorMap.put(token, new ArrayList<Integer>());
							anchorMap.get(token).add(docid);
							
						}
					}
					
					{
						String title = rs.getString("title");
						String[] textParts = Utils.mySplit(title);
						
						List<String> trimedList = new ArrayList<String>();
						for(int j = 0 ; j < textParts.length ; j++){
							String curStr = Utils.myTrimmer(textParts[j]);//textParts[j].trim().replaceAll("^['0-9]+", "").replaceAll("['0-9]+$","");//.replaceAll("'", "''"); 
							if(curStr.length() > 1) trimedList.add(curStr);
						}
	
						//System.out.println(trimedList);
	
						for(int j = 0 ; j < trimedList.size() ; j++){
							String token = trimedList.get(j);
							token = token.trim();
	
							//Add
							if(titleMap.get(token) == null) titleMap.put(token, new ArrayList<Integer>());
							titleMap.get(token).add(docid);
							
						}
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

		idx.setAnchor(anchorMap);
		idx.setTitle(titleMap);
		
		return idx;
	}
	

	public static void pushInvertedIndex(InvertedIndexData  idx, int nGram){
		// TODO Auto-generated method stub
		Map<String, Map<Integer, Integer>> TF = idx.getTF();
		Map<String, Map<Integer, Integer>> DF = idx.getDF();
		Map<String, Map<Integer, Double>> TFIDF = idx.getTFIDF();
		Map<String, Map<Integer, String>> Pos = idx.getPos();

		if(isDebug)
			System.out.println("Push Inverted Index Start!");

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

			//statement.executeUpdate("create table invertedIndex (id integer, word string, docid integer, tf integer, ngram integer)");

			int max = -1;
			int cur = 0;
			for(String word : TF.keySet()){
				cur++;
				if(isDebug && cur%100 == 0)
					System.out.println("<"+cur+", "+TF.keySet().size()+">");
				for(Integer docid : TF.get(word).keySet()){
					int tf = TF.get(word).get(docid);
					int df = DF.get(word).get(docid);
					double tfidf = TFIDF.get(word).get(docid);
					String pos = Pos.get(word).get(docid);
					String exeQuery = "insert into invertedIndex values(null, '"+ word.replace("'",  "''") +"', "+docid+", "+tf+","+nGram+","+df+", "+tfidf+", '"+pos+"')";
//					if(isDebug)
//						System.out.println(exeQuery);
					statement.executeUpdate(exeQuery);
				}

				if(max > 0 && cur == max) break;				
			}
			
			connection.setAutoCommit(true);
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
	
	public static void pushInvertedIndexAnchor(InvertedIndexData  idx, Map<String, Integer> df){
		// TODO Auto-generated method stub
		
		Map<String, List<Integer>> anchorMap = idx.getAnchor();
		Map<String, List<Integer>> titleMap = idx.getTitle();
		int averageDF = 65; //Precomputed 
		
		if(isDebug)
			System.out.println("Push Inverted Index Anchor Start!");

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

			//statement.executeUpdate("create table invertedIndex (id integer, word string, docid integer, tf integer, ngram integer)");

			int max = -1;
			int cur = 0;
			String type = "anchor";
			for(String word : anchorMap.keySet()){
				Integer wordFrequency = df.get(word);
				if(wordFrequency == null) wordFrequency = averageDF;
				cur++;
				if(isDebug && cur%100 == 0)
					System.out.println("<"+cur+", "+anchorMap.keySet().size()+">");
				for(Integer docid : anchorMap.get(word)){
					String exeQuery = "insert into invertedIndexAnchor values(null, '"+ word.replace("'",  "''") +"', "+docid+", "+wordFrequency+",'"+type+"')";
					statement.executeUpdate(exeQuery);
				}

				if(max > 0 && cur == max) break;				
			}
			
			type = "title";
			for(String word : titleMap.keySet()){
				Integer wordFrequency = df.get(word);
				if(wordFrequency == null) wordFrequency = averageDF;
				cur++;
				if(isDebug && cur%100 == 0)
					System.out.println("<"+cur+", "+titleMap.keySet().size()+">");
				for(Integer docid : titleMap.get(word)){
					String exeQuery = "insert into invertedIndexAnchor values(null, '"+ word.replace("'",  "''") +"', "+docid+", "+wordFrequency+",'"+type+"')";
					statement.executeUpdate(exeQuery);
				}

				if(max > 0 && cur == max) break;				
			}
			
			connection.setAutoCommit(true);
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

	public static void printTF(Map<String, Map<Integer, Integer>> invertedIndex){
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

	public static void printTFIDF(Map<String, Map<Integer, Double>> invertedIndex){
		for(String word : invertedIndex.keySet()){
			System.out.println(word);
			Map<Integer, Double> docIdTF = invertedIndex.get(word);
			docIdTF = Utils.sortByValueDouble(docIdTF);
			for(Integer docId : docIdTF.keySet()){

				System.out.println("\t"+"docId: "+docId+", TFIDF: "+docIdTF.get(docId));
			}
			System.out.println("");
		}
	}

	public static void printAll(InvertedIndexData idx){
		printAll(idx.getTF(), idx.getDF(), idx.getTFIDF(), idx.getPos());
	}

	public static void printAll(
			Map<String, Map<Integer, Integer>> TF,
			Map<String, Map<Integer, Integer>> DF,
			Map<String, Map<Integer, Double>> TFIDF,
			Map<String, Map<Integer, String>> wordPos
			){
		for(String word : TF.keySet()){
			System.out.println(word);
			Map<Integer, Integer> docIdTF = TF.get(word);
			docIdTF = Utils.sortByValueInt(docIdTF);
			for(Integer docId : docIdTF.keySet()){
				System.out.println("\t"+"docId: "+docId+", TF: "+docIdTF.get(docId)+", DF: "+DF.get(word).get(docId)+", TFIDF: "+
						TFIDF.get(word).get(docId)+", wordPos :"+wordPos.get(word).get(docId));
			}
			System.out.println("");
		}
	}

	public static void printWordPos(Map<String, Map<Integer, List<Integer>>> invertedIndex){
		for(String word : invertedIndex.keySet()){
			System.out.println(word);
			Map<Integer, List<Integer>> docIdTF = invertedIndex.get(word);
			//docIdTF = Utils.sortByValueInt(docIdTF);
			for(Integer docId : docIdTF.keySet()){

				System.out.println("\t"+"docId: "+docId+", wordPos: "+docIdTF.get(docId));
			}
			System.out.println("");
		}
	}

	public static Map<String, Integer> readDocumentFrequency(){
		Map<String, Integer> dfMap = new TreeMap<String, Integer>();
		String word;
		int df;
		int step = 10000;
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
					word = rs.getString("word");
					df = rs.getInt("df");
					dfMap.put(word, df);
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
		return dfMap;
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

					tfidf = Utils.computeTFIDF(tf,  numDocs,  df);//( 1 + Math.log(tf) ) * Math.log( (double)numDocs / df );

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

			//statement.executeUpdate("alter table invertedIndex add column tfidf double"); // Do this just once

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

		//Update anchor, title inverted index
		Map<String, Integer> df = readDocumentFrequency();
		InvertedIndexData idxAnchor = computeInvertedIndexAnchor();
		pushInvertedIndexAnchor(idxAnchor, df);
		
		//if(1==1) return;
		
//		long startTime = System.currentTimeMillis();
//		long endTime1, endTime3;
//		long endTime2 = startTime;
//
//		Map<String, Integer> df = readDocumentFrequency();
//
//		for(int nGram = 1 ; nGram <= 1 ; nGram++)
//		{
//			InvertedIndexData idx = computeInvertedIndex(nGram, stopWords, df);
//			endTime1 = System.currentTimeMillis();
//
//			endTime2 = System.currentTimeMillis();
//			long lTime_for_read = endTime1 - startTime;
//			long lTime_for_invertedIndex = endTime2 - startTime;
//			System.out.println("TIME (read) : " + lTime_for_read + "(ms)");
//			System.out.println("TIME (invertedIndex) : " + lTime_for_invertedIndex + "(ms)");
//			pushInvertedIndex(idx, nGram);
//			//printAll(idx);
//		}

	


	}



}
