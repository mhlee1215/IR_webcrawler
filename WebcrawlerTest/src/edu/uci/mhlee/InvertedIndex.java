package edu.uci.mhlee;

import java.util.List;
import java.util.Map;

public class InvertedIndex {
	Map<String, Map<Integer, Double>> TFIDF;
	Map<String, Map<Integer, List<Integer>>> Pos;
	
	public InvertedIndex(){
		
	}

	public Map<String, Map<Integer, Double>> getTFIDF() {
		return TFIDF;
	}

	public void setTF(Map<String, Map<Integer, Double>> tFIDF) {
		TFIDF = tFIDF;
	}

	public Map<String, Map<Integer, List<Integer>>> getPos() {
		return Pos;
	}

	public void setPos(Map<String, Map<Integer, List<Integer>>> pos) {
		Pos = pos;
	}
	
	
}
