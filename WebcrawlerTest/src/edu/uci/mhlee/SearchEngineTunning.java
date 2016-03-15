package edu.uci.mhlee;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import edu.uci.mhlee.domains.SearchParams;
import tunning.TunningParams;

public class SearchEngineTunning {
	static Map<Integer, Double> pRank;
	static boolean isDebug = true;
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}

	public static void main(String[] args) {
		SearchEngine se = new SearchEngine();
		//tring query = "Minhaeng";
		int rankType = 2;
//		double anchorWeight = 10000;
//		double titleWeight = 10000;
//		double pageRankWeight = 100;
//		double pageRankMax = 5;
		double pageRankInit = 0.5;
		PrintWriter fw = null;
		try {
			fw = new PrintWriter(new FileWriter("TunningLogs/logs.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			//pRank = PageRank.readPageRank();
			
			double bestndcg = 0.0;
			SearchParams bestParams = null;
			//Scanner keyboard = new Scanner(System.in);
			
			NumberFormat formatter = new DecimalFormat("#0.000");
			int total = TunningParams.getTotalNumberOfCases();
			int cnt = 0;
			for(Double anchorWeight : TunningParams.anchorW){
				for(Double titleWeight : TunningParams.titleW){
					for(Double pageRankWeight : TunningParams.pageRankW){
						for(Double pageRankMax : TunningParams.pageRankMax){
							
							
							
							System.out.println("["+cnt+"/"+total+"]\tCurrent Score "+formatter.format(bestndcg)+"\tCurrent Best ("+formatter.format(bestndcg)+"):\t"+bestParams);
							fw.println("["+cnt+"/"+total+"] Current Score "+bestndcg+"Current Best ("+bestndcg+"):"+bestParams);
							fw.flush();
							cnt++;
							double curndcg = 0.0;
							SearchParams params = new SearchParams(anchorWeight, titleWeight, pageRankWeight, pageRankMax, pageRankInit);
							
							
							for(String query : TunningParams.queries){
								
							
								Map<Integer, Double> docScoreMap = se.getQueryResult(query, statement, rankType, params);
//								// Print 
//								print(docScoreMap);
							

								// Calculate NDCG Score
								{
									// Get GoogleList
									ArrayList<String> googleList = TunningParams.googleResults.get(query);//se.getTopURLsFromGoogle(query);
									
									// Get GobuciList
									ArrayList<String> gobuciList = new ArrayList<String>();
									for(Integer key : docScoreMap.keySet())
									{
										String curQuery = "select url from webContents where docid = "+key;
										ResultSet rs = statement.executeQuery(curQuery);
										String url;
										int cnt2 = 0;
										while (rs.next())
										{
											url = rs.getString("url");
											gobuciList.add(url);
											if (cnt2 == 5) break;
										}
									}
									
									double NDCG5 = se.computeNDCG5(googleList, gobuciList);
									// Print NDCG@5 Score
									//System.out.println("NDCG@5: "+NDCG5);
									System.out.print(formatter.format(NDCG5)+"\t");
									fw.print(formatter.format(NDCG5)+"\t");
									curndcg += NDCG5;
								}
								
							}
							
							System.out.println("");
							fw.println("");
							curndcg /= TunningParams.queries.size();
//							System.out.println(");
							if(curndcg > bestndcg){
								bestndcg = curndcg;
								bestParams = params;
								
							}
						}	
					}	
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
			fw.flush();
			fw.close();
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
