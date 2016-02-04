package p1;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Part B
 * @author mhlee
 *
 */

public class WordFrequencies {
	public static Map<MyToken, Integer> computeWordFrequencies(List<MyToken> tokenList){
		Map<MyToken, Integer> wordFrequencies = new TreeMap<MyToken, Integer>();
		
		for(MyToken tkn : tokenList){
			if(wordFrequencies.get(tkn) == null){
				wordFrequencies.put(tkn, 1);
			}else{
				wordFrequencies.put(tkn, wordFrequencies.get(tkn)+1);
			}
		}
		wordFrequencies = Utilities.sortByValue(wordFrequencies);
		return wordFrequencies;
	}
	
	public static void print(Map<MyToken, Integer> wordFrequencies){
		int j = 0;
		for(MyToken key : wordFrequencies.keySet()){
			System.out.println(key+"/"+wordFrequencies.get(key));
			j++;
			if(j > 10) break;
		}
	}
	
	
	public static void main(String[] argv){
		List<MyToken> tokenList = Utilities.tokenizeFile("pg100.txt");
		Map<MyToken, Integer> wordFrequencies = computeWordFrequencies(tokenList);
		print(wordFrequencies);
		
//		Map<Token, Integer> a = new HashMap<Token, Integer>();
//		a.put(new Token("aa"), 10);
//		System.out.println(a.get(new Token("aa")));
		
	}
	
}
