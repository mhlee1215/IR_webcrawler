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

public class InvertedTable {
	static boolean isDebug = true;
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
		Map<String, Map<Integer, Integer>> invertedIndex = new HashMap<String, Map<Integer, Integer>>();
		int maxRow = DBUtils.getTotalSize();
		int step = 1000;

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
						String curStr = textParts[j].trim(); 
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

			//statement.executeUpdate("select * from webContents");
			//				      statement.executeUpdate("create table person (id integer, name string)");
			//				      statement.executeUpdate("insert into person values(1, 'leo')");
			int max = -1;
			int cur = 0;
			for(String word : invertedIndex.keySet()){
				cur++;
				if(isDebug)
					System.out.println("<"+cur+", "+invertedIndex.keySet().size()+">");
				for(Integer docid : invertedIndex.get(word).keySet()){
					int frequency = invertedIndex.get(word).get(docid);
					String exeQuery = "insert into invertedIndex values(null, '"+word.replace("'", "''")+"', "+docid+", "+frequency+","+nGram+")";
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

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		List<String> stopWords = Utils.readStopWords("stopwords.txt");
		long startTime = System.currentTimeMillis();
		for(int i = 1 ; i <= 3 ; i++){
			Map<String, Map<Integer, Integer>> invertedIndex = computeInvertedIndex(i, stopWords);
			long endTime1 = System.currentTimeMillis();
			pushInvertedIndex(invertedIndex, i);
			 // End time
		    long endTime2 = System.currentTimeMillis();
		    // Total time
		    long lTime_for_read = endTime1 - startTime;
		    long lTime_for_all = endTime2 - startTime;
		    System.out.println("TIME (read) : " + lTime_for_read + "(ms)");
		    System.out.println("TIME (all) : " + lTime_for_all + "(ms)");	
		}
		
		
	}
	
	

}
