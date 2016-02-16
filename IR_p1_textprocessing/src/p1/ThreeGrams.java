package p1;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Part C
 * @author mhlee
 *
 */

public class ThreeGrams {
	
	public static Map<MyToken, Integer> ThreeGramTokenizer(List<MyToken> singleTokenList){
		Map<MyToken, Integer> threeGramFrequencies = new TreeMap<MyToken, Integer>();
		
		for(int i = 0 ; i < singleTokenList.size()-2 ; i++){
			MyToken threeGramToken = MyToken.sum(MyToken.sum(singleTokenList.get(i), singleTokenList.get(i+1)), singleTokenList.get(i+2));
			//threeGramTokenList.add(threeGramToken);
			if(threeGramFrequencies.get(threeGramToken) == null){
				threeGramFrequencies.put(threeGramToken, 1);
			}else{
				threeGramFrequencies.put(threeGramToken, threeGramFrequencies.get(threeGramToken)+1);
			}
		}
		threeGramFrequencies = Utilities.sortByValue(threeGramFrequencies);
		
		return threeGramFrequencies;
	}
	
	public static void print(Map<MyToken, Integer> threeGramTokenList){
		int j = 0;
		for(MyToken key : threeGramTokenList.keySet()){
			System.out.println(key+"/"+threeGramTokenList.get(key));
			j++;
			//if(j > 10) break;
		}
	}
	
	public static void main(String[] args) throws IOException{
		List<MyToken> tokenList = Utilities.tokenizeFile("pg100.txt");
		Map<MyToken, Integer> threeGramTokenList = ThreeGramTokenizer(tokenList);
		print(threeGramTokenList);
	}
}
