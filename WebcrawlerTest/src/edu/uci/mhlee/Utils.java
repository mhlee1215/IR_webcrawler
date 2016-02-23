package edu.uci.mhlee;
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

public class Utils {
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
	
	@SuppressWarnings("unchecked")
	public static Map<Integer, Integer> sortByValueInt(Map<Integer, Integer> map) {
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

}
