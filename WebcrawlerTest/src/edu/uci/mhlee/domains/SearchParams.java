package edu.uci.mhlee.domains;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class SearchParams {
	double anchorWeight = 10000;
	double titleWeight = 10000;
	double pageRankWeight = 100;
	double pageRankMax = 5;
	double pageRankInit = 0.5;
	NumberFormat formatter = new DecimalFormat("#0.000");
	
	public SearchParams(double anchorWeight, double titleWeight, double pageRankWeight, double pageRankMax, double pageRankInit){
		this.anchorWeight = anchorWeight;
		this.titleWeight = titleWeight;
		this.pageRankWeight = pageRankWeight;
		this.pageRankMax = pageRankMax;
		this.pageRankInit = pageRankInit;
	}
	
	public double getAnchorWeight() {
		return anchorWeight;
	}
	public void setAnchorWeight(double anchorWeight) {
		this.anchorWeight = anchorWeight;
	}
	public double getTitleWeight() {
		return titleWeight;
	}
	public void setTitleWeight(double titleWeight) {
		this.titleWeight = titleWeight;
	}
	public double getPageRankWeight() {
		return pageRankWeight;
	}
	public void setPageRankWeight(double pageRankWeight) {
		this.pageRankWeight = pageRankWeight;
	}
	public double getPageRankMax() {
		return pageRankMax;
	}
	public void setPageRankMax(double pageRankMax) {
		this.pageRankMax = pageRankMax;
	}
	public double getPageRankInit() {
		return pageRankInit;
	}
	public void setPageRankInit(double pageRankInit) {
		this.pageRankInit = pageRankInit;
	}
	@Override
	public String toString() {
		return "{\"anchorWeight\":\"" + formatter.format(anchorWeight) + "\",\"titleWeight\":\"" + formatter.format(titleWeight)
				+ "\",\"pageRankWeight\":\"" + formatter.format(pageRankWeight) + "\",\"pageRankMax\":\"" + formatter.format(pageRankMax)
				+ "\",\"pageRankInit\":\"" + formatter.format(pageRankInit) + "\"}";
	}
	
	
}
