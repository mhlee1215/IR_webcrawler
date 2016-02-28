package edu.uci.mhlee.domains;

public class InvertedIndexRow {
	int id;
	String word;
	int docid;
	int tf;
	int ngram;
	int df;
	double tfidf;
	String pos;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getDocid() {
		return docid;
	}
	public void setDocid(int docid) {
		this.docid = docid;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	public int getNgram() {
		return ngram;
	}
	public void setNgram(int ngram) {
		this.ngram = ngram;
	}
	public int getDf() {
		return df;
	}
	public void setDf(int df) {
		this.df = df;
	}
	public double getTfidf() {
		return tfidf;
	}
	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	
	
}
