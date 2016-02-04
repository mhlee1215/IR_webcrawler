package p1;

import java.util.Comparator;

public class MyToken implements Comparator<MyToken>, Comparable<MyToken>{
	 public String string;
	 
	 public MyToken(MyToken o){
		 this.string = o.string;
	 }
	 public MyToken(String string){
		 this.string = string;
	 }
	 
	 @Override
	public String toString() {
		// TODO Auto-generated method stub
		return string;
	}

	 @Override
	 public boolean equals(Object o){
		 if((o instanceof MyToken)&&(((MyToken)o).string.equals(string)))
			 return true;
		 else
			 return false;
	}
	 
	 @Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return string.length();
	}
	 
	@Override
	public int compareTo(MyToken o) {
		// TODO Auto-generated method stub
		return string.compareTo(o.string);
	}

	@Override
	public int compare(MyToken o1, MyToken o2) {
		// TODO Auto-generated method stub
		return o1.string.compareTo(o2.string);
	}
	
	public static MyToken sum(MyToken o1, MyToken o2){
		MyToken a = new MyToken(o1);
		a.string+=" "+o2.string;
		return a;
	}
	
	public char[] toCharArray(){
		return this.string.toCharArray();
	}
	 
	
}
