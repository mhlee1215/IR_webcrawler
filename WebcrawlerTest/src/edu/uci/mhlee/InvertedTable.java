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
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public static Map<String, Integer> computeWordFrequency(int nGram, List<String> stopWords){
		Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
		int maxRow = DBUtils.getTotalSize();
		int step = 10000;

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
				System.out.println(curQuery);
				ResultSet rs = statement.executeQuery(curQuery);
				while(rs.next())
				{
					// read the result set
					String text = rs.getString("text");
					//String[] textParts = text.trim().toLowerCase().split("\\s");
					String[] textParts = text.trim().toLowerCase().split("[^a-z']");
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
						if(wordFrequency.get(token) == null)
							wordFrequency.put(token, 0);

						wordFrequency.put(token, wordFrequency.get(token)+1);
						//System.out.println(token+", "+(wordFrequency.get(token)+1));

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

		wordFrequency = Utils.sortByValue(wordFrequency);

		return wordFrequency;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(DBUtils.getTotalSize());
	}

}
