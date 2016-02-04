import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class WordFrequency {
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}



	public static List<String> readStopWords(String textFile){
		List<String> stopWords = new ArrayList<String>();

		String currentLine = null;
		try{
			FileReader fr = new FileReader(textFile);
			BufferedReader br = new BufferedReader(fr);
			int j = 0;
			while((currentLine = br.readLine()) != null){
				stopWords.add(currentLine.trim());
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}

		return stopWords;
	}

	public static int getTotalSize(){
		int total = 0;


		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			Statement statement2 = connection.createStatement();
			statement2.setQueryTimeout(30);  // set timeout to 30 sec.

			//statement.executeUpdate("select * from webContents");
			//		      statement.executeUpdate("create table person (id integer, name string)");
			//		      statement.executeUpdate("insert into person values(1, 'leo')");
			//		      statement.executeUpdate("insert into person values(2, 'yui')");
			ResultSet rs = statement.executeQuery("select count(*) as count from webContents");
			while(rs.next())
			{
				total = rs.getInt("count");
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

		return total;
	}

	public static void pushWordFrequency(Map<String, Integer> wordFrequency, int nGram){
		
	}
	
	public static Map<String, Integer> computeWordFrequency(int nGram, List<String> stopWords){
		Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
		int maxRow = getTotalSize();
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
					String[] textParts = text.trim().split("\\s");
					List<String> trimedList = new ArrayList<String>();
					for(int j = 0 ; j < textParts.length ; j++){
						String curStr = textParts[j].trim(); 
						if(curStr.length() > 0) trimedList.add(curStr);
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

		wordFrequency = sortByValue(wordFrequency);

		return wordFrequency;
	}

	public static void print(Map<String, Integer> wordFrequencies){
		int j = 0;
		for(String key : wordFrequencies.keySet()){
			System.out.println(key+"/"+wordFrequencies.get(key));
			j++;
			if(j > 50) break;
		}
	}
	
	/**
	 * sortByValue
	 * @reference : http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Integer> sortByValue(Map<String, Integer> map) {
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	} 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> stopWords = readStopWords("stopwords.txt");
		System.out.println(stopWords);
		System.out.println(getTotalSize());
		Map<String, Integer> wf = computeWordFrequency(1, stopWords);
		print(wf);
	}

}
