import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class sqliteTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Class.forName("org.sqlite.JDBC");
			
			Connection connection = null;
		    try
		    {
		      // create a database connection
		      connection = DriverManager.getConnection("jdbc:sqlite:d:/IR_storage/crawlingData.db");
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.

		      //statement.executeUpdate("select * from webContents");
//		      statement.executeUpdate("create table person (id integer, name string)");
//		      statement.executeUpdate("insert into person values(1, 'leo')");
//		      statement.executeUpdate("insert into person values(2, 'yui')");
		      ResultSet rs = statement.executeQuery("select * from webContents");
		      while(rs.next())
		      {
		        // read the result set
		        System.out.println("docid = " + rs.getString("docid"));
		        System.out.println("id = " + rs.getInt("id"));
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
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	
	}

}
