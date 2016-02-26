import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnector {

	Statement statement = null;
	Connection connection = null;
	
	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public DBConnector(){
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:d:/IR_storage/crawlingData.db");
			statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void close(){
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

	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}
}