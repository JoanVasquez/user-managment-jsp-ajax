package userprocesses.db;

public class DBConnectionProperties {
	private String driver;
	private String user;
	private String password;
	private String url;

	public DBConnectionProperties() {
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String db) {
		this.url = db;
	}

}
