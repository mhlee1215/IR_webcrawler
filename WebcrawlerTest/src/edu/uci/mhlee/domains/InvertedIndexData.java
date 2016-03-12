package edu.uci.mhlee.domains;

import java.util.List;
import java.util.Map;

public class InvertedIndexData {
	Map<String, Map<Integer, Integer>> TF;
	Map<String, Map<Integer, Integer>> DF;
	Map<String, Map<Integer, Double>> TFIDF;
	Map<String, Map<Integer, String>> Pos;
	Map<String, List<Integer>> Anchor;
	Map<String, List<Integer>> Title;
	
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
	public Map<String, List<Integer>> getAnchor() {
		return Anchor;
	}
	public void setAnchor(Map<String, List<Integer>> anchor) {
		Anchor = anchor;
	}
	public Map<String, List<Integer>> getTitle() {
		return Title;
	}
	public void setTitle(Map<String, List<Integer>> title) {
		Title = title;
	}
}
