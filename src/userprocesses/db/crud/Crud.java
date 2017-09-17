package userprocesses.db.crud;

import java.sql.SQLException;
import java.util.List;
//CRUD METHODS
public interface Crud<Entity> {

	boolean insertEntity(Entity e) throws SQLException;

	boolean updateEntity(Entity e) throws SQLException;

	boolean deleteEntity(int id) throws SQLException;

	Entity signIn(Entity e) throws SQLException;

	String forgottenPassword(String email) throws SQLException;

	List<Entity> entities() throws SQLException;;

}
