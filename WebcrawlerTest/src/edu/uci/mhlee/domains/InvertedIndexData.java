package edu.uci.mhlee.domains;

import java.util.List;
import java.util.Map;

public class InvertedIndexData {
	Map<String, Map<Integer, Integer>> TF;
	Map<String, Map<Integer, Integer>> DF;
	Map<String, Map<Integer, Double>> TFIDF;
	Map<String, Map<Integer, String>> Pos;
	
	public Map<String, Map<Integer, Integer>> getDF() {
		return DF;
	}
	public void setDF(Map<String, Map<Integer, Integer>> dF) {
		DF = dF;
	}
	public Map<String, Map<Integer, Integer>> getTF() {
		return TF;
	}
	public void setTF(Map<String, Map<Integer, Integer>> tF) {
		TF = tF;
	}
	public Map<String, Map<Integer, Double>> getTFIDF() {
		return TFIDF;
	}
	public void setTFIDF(Map<String, Map<Integer, Double>> tFIDF) {
		TFIDF = tFIDF;
	}
	public Map<String, Map<Integer, String>> getPos() {
		return Pos;
	}
	public void setPos(Map<String, Map<Integer, String>> pos) {
		Pos = pos;
	}
	
	
	
	
}
