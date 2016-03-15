package tunning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import edu.uci.mhlee.SearchEngine;

public class TunningParams {
	public static Vector<String> queries = new Vector<String>();
	public static Vector<Double> anchorW = new Vector<Double>();
	public static Vector<Double> titleW = new Vector<Double>();
	public static Vector<Double> pageRankW = new Vector<Double>();
	public static Vector<Double> pageRankMax = new Vector<Double>();
	public static Map<String, ArrayList<String>> googleResults = new HashMap<String, ArrayList<String>>();
	
//	double anchorWeight = 10000;
//	double titleWeight = 10000;
//	double pageRankWeight = 100;
//	double pageRankMax = 5;
//	double pageRankInit = 0.5;
	
	static{
		queries.add("mondego");
		queries.add("machine learning");
		queries.add("software engineering");
		queries.add("security");
		queries.add("student affairs");
		queries.add("graduate courses");
		queries.add("Crista Lopes");
		queries.add("REST");
		queries.add("computer games");
		queries.add("information retrieval");
		
		for(double i = -5 ; i < 7 ; i+=2){
			anchorW.add(Math.pow(10, i/2));
			titleW.add(Math.pow(10, i/2));
			pageRankW.add(Math.pow(10, i/2));
			pageRankMax.add(Math.pow(10, i/2));
		}
		for(int i = 0 ; i < queries.size(); i++){
			//System.out.println("Get google results.."+i);
			googleResults.put(queries.get(i), SearchEngine.getTopURLsFromGoogle(queries.get(i)));
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
	}
	
	public static int getTotalNumberOfCases(){
		return anchorW.size()*titleW.size()*pageRankW.size()*pageRankMax.size();
	}
	
	
	
	public static void main(String[] args){
		System.out.println(queries);
		System.out.println(anchorW);
		System.out.println(titleW);
		System.out.println(pageRankW);
		System.out.println(pageRankMax);
	}
}
