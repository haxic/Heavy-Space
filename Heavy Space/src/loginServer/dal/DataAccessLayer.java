package loginServer.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataAccessLayer {
	private static final String ENDPOINT = "jdbc:postgresql://127.0.0.1:5432/heavyspace";
	private static final String USERNAME = "loginserver";
	private static final String PASSWORD = "loginserver";

	Connection dbc;
	IAccountDAO accountDAO;

	public DataAccessLayer() {
		System.out.println("-------- PostgreSQL " + "JDBC Connection Testing ------------");
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path! " + e.getMessage());
			return;
		}
		System.out.println("PostgreSQL JDBC Driver Registered!");
		dbc = null;
		try {
			dbc = DriverManager.getConnection(ENDPOINT, USERNAME, PASSWORD);
		} catch (SQLException e) {
			System.out.println("Connection Failed! " + e.getMessage());
			return;
		}
		if (dbc != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
			return;
		}

		try {
			dbc.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		accountDAO = new AccountDAO(dbc);
	}

	public IAccountDAO getAccountDAO() {
		return accountDAO;
	}
}
