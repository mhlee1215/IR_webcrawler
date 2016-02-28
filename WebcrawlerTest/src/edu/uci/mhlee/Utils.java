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
	
	@SuppressWarnings("unchecked")
	public static Map<Integer, Double> sortByValueDouble(Map<Integer, Double> map) {
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
	
	public static String[] mySplit(String text){
		//return text.trim().toLowerCase().split("[^a-z']");
		return text.trim().split("[^a-zA-Z']");
		//return text.trim().replaceAll("^['0-9]+", "").replaceAll("['0-9]+$","").replaceAll("'", "''"); 
	}
	
	public static String myTrimmer(String text){
		return text.trim().replaceAll("^['0-9]+", "").replaceAll("['0-9]+$","");//.replaceAll("'", "''");
	}
	
	public static double computeTFIDF(int tf, int N, int df){
		return ( 1 + Math.log(tf) ) * Math.log( (double)N / df );
	}
	
	public static String neighborPosString(String pos1, String pos2, int gap){
		String posStr = "";
		
		String[] parts1 = pos1.split(",");
		String[] parts2 = pos2.split(",");
		
		for(String part1Str : parts1){
			int part1Int = Integer.parseInt(part1Str.trim());
			for(String part2Str : parts2){
				int part2Int = Integer.parseInt(part2Str.trim());
				if(part2Int-part1Int <= gap && part2Int-part1Int >= 0){
					//System.out.println(part2Int+"///"+part1Int +" /// "+(part2Int-part1Int)+" ///"+gap);
					if(posStr.length() == 0)
						posStr = Integer.toString(part2Int);
					else
						posStr += ", "+Integer.toString(part2Int);
				}
			}	
		}
		
		return posStr;
		
	}

}
