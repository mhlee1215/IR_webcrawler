package p1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Part A
 * @author mhlee
 *
 */

public class Utilities {
	public static List<MyToken> tokenizeFile(String textFile){
		List<MyToken> tokenList = new ArrayList<MyToken>();
		
		String currentLine = null;
		try{
			FileReader fr = new FileReader(textFile);
			BufferedReader br = new BufferedReader(fr);
			int j = 0;
			while((currentLine = br.readLine()) != null){
		        String[] stParts = currentLine.trim().split("[^a-zA-Z0-9]");
		        for(int i = 0 ; i < stParts.length ; i++){
		        	if(stParts[i].length() == 0) continue;
		        	tokenList.add(new MyToken(stParts[i].toLowerCase()));
		        }
		        j++;
		        
		        //if(j > 10) break;
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return tokenList;
	}
	
	public static void print(List<MyToken> tokenList){
		for(MyToken tkn : tokenList){
			System.out.println(tkn);
		}
	}
	
	/**
	 * sortByValue
	 * @reference : http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<MyToken, Integer> sortByValue(Map<MyToken, Integer> map) {
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
	
	public static void main(String[] argv){
		List<MyToken> tokenList = Utilities.tokenizeFile("pg100.txt");
		//System.out.println(tokenList);
		Utilities.print(tokenList);
	}
	
}
