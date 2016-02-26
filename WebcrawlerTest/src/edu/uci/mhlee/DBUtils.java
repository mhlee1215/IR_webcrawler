package edu.uci.mhlee;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils {
	static PropertyReader pr = null;
	static {
		try {
			pr = new PropertyReader();
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	
	
	public static int getTotalSize(){
		int total = 0;


		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+pr.getDbPath());
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			Statement statement2 = connection.createStatement();
			statement2.setQueryTimeout(30);  // set timeout to 30 sec.

			//statement.executeUpdate("select * from webContents");
			//		      statement.executeUpdate("create table person (id integer, name string)");
			//		      statement.executeUpdate("insert into person values(1, 'leo')");
			//		      statement.executeUpdate("insert into person values(2, 'yui')");
			ResultSet rs = statement.executeQuery("select count(*) as count from webContents");
			while(rs.next())
			{
				total = rs.getInt("count");
			}
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		}
		finally
		{
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}

		return total;
	}
}
