package shared.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import shared.idal.IAccountDAO;
import shared.idal.IAuthenticationTokenDAO;
import shared.idal.IDataAccessLayer;
import shared.idal.IGameServerDAO;

public class DataAccessLayer implements IDataAccessLayer {
	private static final String ENDPOINT = "jdbc:postgresql://ec2-23-21-92-251.compute-1.amazonaws.com/d4jfrp7pjrtdjh";
	private static final String USERNAME = "fbqkxcdwyqdbcj";
	private static final String PASSWORD = "6d89f6eea619b383f076c82d1da8bfd0d784ef381648b0021ceb63467ca0b1ad";

	Connection dbc;
	IAccountDAO accountDAO;
	IAuthenticationTokenDAO authenticationTokenDAO;
	IGameServerDAO gameServerDAO;

	public DataAccessLayer() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path! " + e.getMessage());
			return;
		}
		dbc = null;
		try {
			Properties props = new Properties();
			props.setProperty("user", USERNAME);
			props.setProperty("password", PASSWORD);
			props.setProperty("ssl", "true");
			props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
			dbc = DriverManager.getConnection(ENDPOINT, props);
		} catch (SQLException e) {
			System.out.println("Connection Failed! " + e.getMessage());
			return;
		}
		if (dbc == null) {
			System.out.println("Failed to make connection!");
			return;
		}

		/*
		 * try {
		 * dbc.setAutoCommit(false);
		 * } catch (SQLException e) {
		 * System.out.println("Disabling database auto commit failed! " + e.getMessage());
		 * return;
		 * }
		 */
		accountDAO = new AccountDAO(dbc);
		authenticationTokenDAO = new AuthenticationTokenDAO(dbc);
		gameServerDAO = new GameServerDAO(dbc);
	}

	@Override
	public IAccountDAO getAccountDAO() {
		return accountDAO;
	}

	public IAuthenticationTokenDAO getAuthenticationTokenDAO() {
		return authenticationTokenDAO;
	}

	@Override
	public IGameServerDAO getGameServerDAO() {
		return gameServerDAO;
	}
}
