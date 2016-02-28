package edu.uci.mhlee;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			Scanner keyboard = new Scanner(System.in);

			
			while(!"exit".equals(query.toLowerCase())){//query.length() >= 0){
				Map<Integer, Double> tfidfMap = new HashMap<Integer, Double>();
				Map<Integer, String> posMap = null;//new HashMap<Integer, String>();
				
				System.out.print("Plase enter query: ");
				query = keyboard.nextLine();
				String[] queryParts = query.split(" ");

				String word;
				int tf, df, docid;
				double tfidf;
				String pos;

				int queryCnt = 0;
				for(String queryWord : queryParts){
					Map<Integer, String> curPosMap = new HashMap<Integer, String>();
					// First load DFs into dfMap

					String curQuery = "select word, docid, tf, df, tfidf, pos from invertedIndex where word = '"+queryWord+"'";
					//System.out.println(curQuery);
					ResultSet rs = statement.executeQuery(curQuery);

					while (rs.next())
					{
						// read the result set
						word = rs.getString("word");
						docid = rs.getInt("docid");
						tf = rs.getInt("tf");
						df = rs.getInt("df");
						tfidf = rs.getDouble("tfidf");
						pos = rs.getString("pos");


						if(tfidfMap.get(docid) == null) tfidfMap.put(docid, 0.0);
						//Individual adding
						if(rankType == 1){
							tfidfMap.put(docid, tfidfMap.get(docid)+tfidf);	
						}else if(rankType == 2){
							//Just copy only for the first query word
							if(queryCnt==0){
								curPosMap.put(docid, pos);
								tfidfMap.put(docid, tfidfMap.get(docid)+tfidf);	
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

									//								if(docid == 3465){
									//									System.out.println(queryWord+"//// "+matchedPos);
									//									System.out.println("posMap.get(docid):" +posMap.get(docid));
									//									System.out.println("pos:" +pos);
									//								}
									tfidfMap.put(docid, tfidfMap.get(docid)+tfidf*(Math.log(1+matchedPosCnt)+1));

									//log(1+matchedPosCnt)+1
								}
							}
						}



					}

					posMap = curPosMap;
					queryCnt++;
				}
				tfidfMap = Utils.sortByValueDouble(tfidfMap);
				print(tfidfMap);
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
			int j = 0;
			NumberFormat formatter = new DecimalFormat("#0.000");     
			for(Integer key : wordFrequencies.keySet()){
				//System.out.println(key+"/"+wordFrequencies.get(key));

				String curQuery = "select docid, url, subdomain, path from webContents where docid = "+key;
				//System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);

				while (rs.next())
				{
					// read the result set
					docid = rs.getInt("docid");
					url = rs.getString("url");
					subdomain = rs.getString("subdomain");
					path = rs.getString("path");

					System.out.println("Rank<"+(j+1)+"> Score:"+formatter.format(wordFrequencies.get(key))+"\tDocID: "+docid+"\tURL: "+url+"\tSubDomain: "+subdomain+"\tPath:"+path);
					
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
