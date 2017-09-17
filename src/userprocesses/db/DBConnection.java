package userprocesses.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//DATABASE CONNECTION CLASS
public class DBConnection {
	private static DBConnection dbConnection; //VAR OF THIS CLASS
	private static Connection connection; //VAR OF CONNECTION CLASS - TO CREATE THE DATABASE CONNECTION

	//PRIVATE CONSTRUCTOR FOR SINGLETON PATTERN
	//dbConnectionProperties receives data about the connection
	private DBConnection(DBConnectionProperties dbConnectionProperties) {
		try {
			Class.forName(dbConnectionProperties.getDriver()); //LOAD THE MYSQL DRIVER
			connection = DriverManager.getConnection(dbConnectionProperties.getUrl(), dbConnectionProperties.getUser(),
					dbConnectionProperties.getPassword()); // CREATE THE DATABASE CONNECTION

		} catch (SQLException ex) {
			System.err.println("Connection to the DataBase failed " + ex.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Loading driver failed: " + e.getMessage());
		}
	}

	//SINGLETON METHOD - RETURN AN INSTANCE OF THIS CLASS
	public synchronized static DBConnection openConnection(DBConnectionProperties dbConnectionProperties) {
		if (dbConnection == null) {
			dbConnection = new DBConnection(dbConnectionProperties);
		}
		return dbConnection;
	}

	//RETURN AN INSTANCE OF connection
	public Connection getConnection() {
		return connection;
	}

	//CLOSE THIS CONNECTION
	public void closeConnection() {

		if ((connection != null) && (dbConnection != null)) {
			try {
				connection.close();
				dbConnection = null;
			} catch (SQLException e) {
				System.err.println("Failed to close the connection " + e.getMessage());
			}
		} else {
			System.out.println("Not opened connection");
		}

	}
}
