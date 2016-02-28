package edu.uci.mhlee;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		// TODO Auto-generated method stub

		Map<Integer, Double> tfidfMap = new HashMap<Integer, Double>();


		//Scanner keyboard = new Scanner(System.in);

		String query = "computer science";

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.



			//while(query.length() >= 0){
			//query = keyboard.nextLine();
			String[] queryParts = query.split(" ");

			String word;
			int tf, df, docid;
			double tfidf;
			String pos;
			for(String queryWord : queryParts){

				// First load DFs into dfMap

				String curQuery = "select word, docid, tf, df, tfidf, pos from invertedIndex where word = '"+queryWord+"'";
				System.out.println(curQuery);
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
					tfidfMap.put(docid, tfidfMap.get(docid)+tfidf);

				}
			}
			//}

			tfidfMap = Utils.sortByValueDouble(tfidfMap);

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

		print(tfidfMap);		


	}


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
					
					System.out.println("Rank <"+(j+1)+"> Score:"+wordFrequencies.get(key)+", DocID: "+docid+" / URL: "+url+" / SubDomain: "+subdomain+" / Path:"+path);
					System.out.println("");
				}
				
				
				
				j++;
				if(j > 10) break;
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


	}




}
