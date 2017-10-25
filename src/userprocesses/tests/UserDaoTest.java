package userprocesses.tests;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import userprocesses.db.DBConnection;
import userprocesses.db.DBConnectionProperties;
import userprocesses.db.dao.UserDao;
import userprocesses.models.User;
import userprocesses.security.AES256;

//USER TEST CLASS
public class UserDaoTest {

	private static UserDao userDao;// USER DAO
	private static User user;// USER ENTITY

	private DBConnection dbConnection;// DATABASE CONNECTION
	private PreparedStatement prepareStatement;
	private ResultSet resultSet;
	private DBConnectionProperties dbConnectionProperties;
	private int idUser = 0;// USER ID

	@BeforeClass
	public static void setUpClass() {
		user = new User();
	}

	@Test // SAVE USER TEST
	public void test1() throws SQLException {
		userDao = new UserDao();
		user.setEmail("yoloprogramo22@gmail.com");
		user.setFirstName("Test insert");
		user.setLastName("Test insert");
		user.setPassword(AES256.encryption("pass"));
		Assert.assertTrue(userDao.insertEntity(user));
	}

	@Before // GET AN USER ID
	public void test2() throws SQLException {
		dbConnectionProperties = new DBConnectionProperties();
		dbConnectionProperties.setDriver("com.mysql.jdbc.Driver");
		dbConnectionProperties.setUrl("jdbc:mysql://localhost:3306/userprocesses");
		dbConnectionProperties.setUser("root");
		dbConnectionProperties.setPassword("pass");
		dbConnection = DBConnection.openConnection(dbConnectionProperties);

		prepareStatement = dbConnection.getConnection().prepareStatement("SELECT iduser FROM user LIMIT 1");
		resultSet = prepareStatement.executeQuery();
		if (resultSet.next())
			idUser = resultSet.getInt(1);

		resultSet.close();
		prepareStatement.close();
		dbConnection.closeConnection();

	}

	@Test // UPDATE USER TEST
	public void test3() throws SQLException {
		if (idUser > 0) {
			userDao = new UserDao();
			user.setIdUser(idUser);
			user.setFirstName("Test update");
			user.setLastName("Test update");
			user.setPassword(AES256.encryption("pass"));
			Assert.assertTrue(userDao.updateEntity(user));
		}
	}

	@Test // GET ALL OF THE USER TEST
	public void test4() throws SQLException {
		userDao = new UserDao();
		Assert.assertNotEquals(0, userDao.entities());
	}

	@Test // SINGIN TEST
	public void test5() throws SQLException {
		userDao = new UserDao();
		Assert.assertNotEquals(null, userDao.signIn(user));
	}

	@Test // FORGOTTEN PASSWORD TEST
	public void test6() throws SQLException {
		userDao = new UserDao();
		user.setEmail("yoloprogramo22@gmail.com");
		Assert.assertNotEquals(null, userDao.forgottenPassword(user.getEmail()));
	}

	@Test // DELETE USER TEST
	public void test7() throws SQLException {
		System.out.println(idUser);
		if (idUser > 0) {
			userDao = new UserDao();
			Assert.assertTrue(userDao.deleteEntity(idUser));
		}

	}
}
