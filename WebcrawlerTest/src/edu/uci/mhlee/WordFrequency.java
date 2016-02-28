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
import java.util.TreeMap;


public class WordFrequency {
	static boolean isDebug = true;
	
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}



	
	

	public static void pushWordFrequency(Map<String, Integer> wordFrequency, int nGram){
		// TODO Auto-generated method stub


		//Get rank query
		//select (select count(*) from wordFrequency b  where a.id >= b.id and a.ngram = b.ngram) as cnt, 
		//      word, frequency from wordFrequency a where ngram = 3 limit 20
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
			for(String word : wordFrequency.keySet()){
				cur++;
				int frequency = wordFrequency.get(word);
				sql = "insert into wordFrequency values(null, '"+word.replace("'", "''")+"', "+frequency+", "+nGram+")";
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

	public static Map<String, Integer> computeWordFrequency(int nGram, List<String> stopWords){
		Map<String, Integer> wordFrequency = new TreeMap<String, Integer>();
		int maxRow = DBUtils.getTotalSize("webContents");
		int step = 10000;
		int cntt = 0;

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			Statement statement2 = connection.createStatement();
			statement2.setQueryTimeout(30);  // set timeout to 30 sec.

			for(int i = 0 ; i < maxRow ; i+=step){
				String curQuery = "select text from webContents limit "+step+" offset "+i;
				if(isDebug)
					System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);
				while(rs.next())
				{
					// read the result set
					String text = rs.getString("text");
					//String[] textParts = text.trim().toLowerCase().split("\\s");
					String[] textParts = Utils.mySplit(text);
					List<String> trimedList = new ArrayList<String>();
					
					for(int j = 0 ; j < textParts.length ; j++)
					{
						//Changed to function form.
						String curStr = Utils.myTrimmer(textParts[j]); 
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
						if(wordFrequency.get(token) == null)
							wordFrequency.put(token, 0);

						wordFrequency.put(token, wordFrequency.get(token)+1);
//						System.out.println(token+", "+(wordFrequency.get(token)+1));

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

		//wordFrequency = Utils.sortByValue(wordFrequency);
		return wordFrequency;
	}
	
	
	
	/**
	 * 
	 * @param stopWords
	 * @deprecated Too Slow.
	 */
	public static void updateWordCount(List<String> stopWords){
		
		int maxRow = DBUtils.getTotalSize("webContents");
		int step = 10000;

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			Statement statement2 = connection.createStatement();
			statement2.setQueryTimeout(30);  // set timeout to 30 sec.

			for(int i = 0 ; i < maxRow ; i+=step){
				String curQuery = "select id, text from webContents limit "+step+" offset "+i;
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);
				while(rs.next())
				{
					// read the result set
					int id = rs.getInt("id");
					String text = rs.getString("text");
					//String[] textParts = text.trim().toLowerCase().split("\\s");
					String[] textParts = Utils.mySplit(text);
					List<String> trimedList = new ArrayList<String>();
					for(int j = 0 ; j < textParts.length ; j++){
						//Changed to function form.
						String curStr = Utils.myTrimmer(textParts[j]); 
						if(curStr.length() > 1) trimedList.add(curStr);
					}
					
					String updateQuery = "update webContents set wordcount = "+trimedList.size()+" where id = "+id;
					//System.out.println(updateQuery);
					statement2.executeUpdate(updateQuery);
					
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

	public static void print(Map<String, Integer> wordFrequencies){
		int j = 0;
		for(String key : wordFrequencies.keySet()){
			System.out.println(key+"/"+wordFrequencies.get(key));
			j++;
			if(j > 5000) break;
		}
	}

	

	public static void main(String[] args) {

		List<String> stopWords = Utils.readStopWords("stopwords.txt");
//		System.out.println(stopWords);
//		System.out.println(getTotalSize());
		
//		updateWordCount(stopWords);
		
		int nGram = 0;
		
		nGram =1;
		Map<String, Integer> wf = computeWordFrequency(nGram, stopWords);
		pushWordFrequency(wf, nGram);
		//print(wf);
		
//		nGram =3;
//		Map<String, Integer> wf3 = computeWordFrequency(nGram, stopWords);
//		pushWordFrequency(wf3, nGram);
		//print(wf);
	}

}
