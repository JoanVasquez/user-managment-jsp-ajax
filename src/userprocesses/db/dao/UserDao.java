package userprocesses.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import userprocesses.db.DBConnection;
import userprocesses.db.DBConnectionProperties;
import userprocesses.db.crud.Crud;
import userprocesses.db.queries.Queries;
import userprocesses.models.User;
import userprocesses.security.AES256;

//DAO CLASS - IMPLEMENT CRUD INTERFACE
public class UserDao extends Queries implements Crud<User> {

	private DBConnection dbConnection; // VAR OF DBConnection TO OPEN THE CONNECTION
	private PreparedStatement prepareStatement;
	private ResultSet resultSet;
	private DBConnectionProperties dbConnectionProperties; // DATABASE PROPERTIES

	//CONSTRUCTOR
	public UserDao() {
		dbConnectionProperties = new DBConnectionProperties();
		dbConnectionProperties.setDriver("com.mysql.jdbc.Driver");// THE DRIVER - MYSQL IN THIS CASE
		dbConnectionProperties.setUrl("jdbc:mysql://localhost:3306/userprocesses");// DATABASE URL
		dbConnectionProperties.setUser("root");// DATABASE USER
		dbConnectionProperties.setPassword("pass22");// DATABASE PASSWORD
		dbConnection = DBConnection.openConnection(dbConnectionProperties);
	}

	//SAVE USER TO THE DATABASE
	@Override
	public boolean insertEntity(User e) throws SQLException {
		boolean result = false;
		prepareStatement = dbConnection.getConnection().prepareStatement(INSERT_USER);
		prepareStatement.setString(1, e.getFirstName());
		prepareStatement.setString(2, e.getLastName());
		prepareStatement.setString(3, e.getEmail());
		prepareStatement.setBytes(4, e.getPassword());

		if (prepareStatement.executeUpdate() > 0)
			result = true;

		prepareStatement.close();

		return result;
	}

	//UPDATE USER
	@Override
	public boolean updateEntity(User e) throws SQLException {
		boolean result = false;
		prepareStatement = dbConnection.getConnection().prepareStatement(UPDATE_USER);
		prepareStatement.setString(1, e.getFirstName());
		prepareStatement.setString(2, e.getLastName());
		prepareStatement.setBytes(3, e.getPassword());
		prepareStatement.setInt(4, e.getIdUser());

		if (prepareStatement.executeUpdate() > 0)
			result = true;

		prepareStatement.close();

		return result;
	}

	//DELETE USER
	@Override
	public boolean deleteEntity(int id) throws SQLException {
		boolean result = false;
		prepareStatement = dbConnection.getConnection().prepareStatement(DELETE_USER);
		prepareStatement.setInt(1, id);

		if (prepareStatement.executeUpdate() > 0)
			result = true;

		prepareStatement.close();

		return result;
	}

	//LOGIN OR SIGNIN FOR THE USER
	@Override
	public User signIn(User e) throws SQLException {
		prepareStatement = dbConnection.getConnection().prepareStatement(SIGN_IN);
		prepareStatement.setString(1, e.getEmail());
		prepareStatement.setBytes(2, e.getPassword());

		resultSet = prepareStatement.executeQuery();
		if (resultSet.next()) {
			e.setIdUser(resultSet.getInt(1));
			e.setFirstName(resultSet.getString(2));
			e.setLastName(resultSet.getString(3));
			e.setEmail(resultSet.getString(3));
			e.setPassword(resultSet.getBytes(4));
		} else {
			e = null;
		}

		prepareStatement.close();

		return e;
	}

	//GET THE PASSWORD OF AN SPECIFY USER
	@Override
	public String forgottenPassword(String email) throws SQLException {
		String password = null;
		prepareStatement = dbConnection.getConnection().prepareStatement(FORGOTTEN_PASSWORD);
		prepareStatement.setString(1, email);

		resultSet = prepareStatement.executeQuery();
		if (resultSet.next())
			password = AES256.decryption(resultSet.getString(1));

		prepareStatement.close();

		return password;
	}

	//GET A LIST OF ALL OF THE USERS
	@Override
	public List<User> entities() throws SQLException {
		List<User> listUser = new ArrayList<User>();

		prepareStatement = dbConnection.getConnection().prepareStatement(READ_USERS);

		resultSet = prepareStatement.executeQuery();

		while (resultSet.next()) {
			User user = new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3),
					resultSet.getString(4), AES256.decryption(resultSet.getString(5)));
			listUser.add(user);
		}

		prepareStatement.close();

		return listUser;
	}

	//CLOSE THE CONNECTION
	public void closeConnection() {
		dbConnection.closeConnection();
	}

}
