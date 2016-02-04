package p1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Part D
 * @author mhlee
 * 
 * All we have to do is word-wise sorting.
 * O(mnlogn)
 *   n = Average length of word
 *   m = number of words *
 */

public class Anagrams {
	public static Map<MyToken, List<MyAnagram>> detectAnagrams(List<MyToken> tokenList){
		Map<MyToken, List<MyAnagram> > mapAnagram = new TreeMap<MyToken, List<MyAnagram>>();
		Map<MyToken, List<MyToken> > mapSortedAnagram = new HashMap<MyToken, List<MyToken>>();
		
		for(MyToken myToken : tokenList){
			char[] charArray = myToken.toCharArray(); 
			Arrays.sort(charArray); 
			MyToken sortedWord = new MyToken(new String(charArray));
			
			if(mapSortedAnagram.get(sortedWord) == null){
				mapSortedAnagram.put(sortedWord, new ArrayList<MyToken>());	
			}
			if(!mapSortedAnagram.get(sortedWord).contains(myToken))
				mapSortedAnagram.get(sortedWord).add(myToken);
		}
		for(MyToken token : mapSortedAnagram.keySet()){
			List<MyToken> anagramTokenList = mapSortedAnagram.get(token);
			if(anagramTokenList.size() == 1 ) continue;
			for(int i = 0 ; i < anagramTokenList.size() ; i++){
				MyToken curToken = anagramTokenList.get(i);
				if(mapAnagram.get(curToken) == null){
					mapAnagram.put(curToken, new ArrayList<MyAnagram>());	
				}
				for(int j = 0 ; j < anagramTokenList.size() ; j++){
					MyToken curToken2 = anagramTokenList.get(j);
					if(i == j) continue;
					
					mapAnagram.get(curToken).add(new MyAnagram(curToken2));
				}	
			}
		}
		return mapAnagram;
	}
	
	public static void print(Map<MyToken,List<MyAnagram> > mapAnagram){
		for(MyToken key : mapAnagram.keySet()){
			List<MyAnagram> listAnagram = mapAnagram.get(key);
			System.out.print("<"+key+"> - ");
			Collections.sort(listAnagram);
			System.out.print(listAnagram);
			System.out.println("");
		}
	}
	
	public static void main(String[] args){
		List<MyToken> tokenList = Utilities.tokenizeFile("pg100.txt");
		Map<MyToken, List<MyAnagram> > mapAnagram = detectAnagrams(tokenList);
		//System.out.println(mapAnagram);
		print(mapAnagram);
	}
	
}
