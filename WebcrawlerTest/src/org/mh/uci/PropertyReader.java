package org.mh.uci;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
 
/**
 * @author Crunchify.com
 * 
 */
 
public class PropertyReader {
	String dbPath = "";
	InputStream inputStream;
 
	public void getPropValues() throws IOException {
 
		try {
			Properties prop = new Properties();
			String propFileName = "config.properties";
 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
			Date time = new Date(System.currentTimeMillis());
 
			// get the property value and print it out
			this.dbPath = prop.getProperty("dbPath");
			
 
	//		result = "Company List = " + company1 + ", " + company2 + ", " + company3;
			//System.out.println("dbPath: "+dbPath);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
	}
	
	public PropertyReader(){
		try {
			getPropValues();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getDbPath() {
		return dbPath;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public static void main(String[] args){
		PropertyReader pr = new PropertyReader();
	}
}