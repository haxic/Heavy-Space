package tests.dbsetup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import shared.dal.AccountDAO;
import shared.dal.AuthenticationTokenDAO;
import shared.dal.GameServerDAO;
import shared.idal.IAccountDAO;
import shared.idal.IAuthenticationTokenDAO;
import shared.idal.IDataAccessLayer;
import shared.idal.IGameServerDAO;

public class DBTestSetup implements IDataAccessLayer {
	private static final String ENDPOINT = "jdbc:postgresql://127.0.0.1:5432/testdb";
	private static final String USERNAME = "haxic";
	private static final String PASSWORD = "";

	private Connection dbc;
	IAccountDAO accountDAO;
	IAuthenticationTokenDAO authenticationTokenDAO;
	IGameServerDAO gameServerDAO;

	public DBTestSetup() {
		connect();
		clean();
		setup();
		this.accountDAO = new AccountDAO(dbc);
		this.authenticationTokenDAO = new AuthenticationTokenDAO(dbc);
		this.gameServerDAO = new GameServerDAO(dbc);
	}

	public void connect() {
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
			dbc = DriverManager.getConnection(ENDPOINT, props);
		} catch (SQLException e) {
			System.out.println("Connection Failed! " + e.getMessage());
			return;
		}
		if (dbc == null) {
			System.out.println("Failed to make connection!");
			return;
		}
		System.out.println("Connected to testdb!");
	}

	public void clean() {
		try {
			Statement s = dbc.createStatement();
			String sql = "DROP SCHEMA public CASCADE;";
			s.executeUpdate(sql);
			// System.out.println("Schema dropped.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			Statement s = dbc.createStatement();
			String sql = "CREATE SCHEMA public;";
			s.executeUpdate(sql);
			// System.out.println("Schema created.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setup() {
		String sql = "";
		{
			String columns = "id SERIAL PRIMARY KEY," + "username VARCHAR(100) NOT NULL UNIQUE," + "password VARCHAR(100) NOT NULL," + "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
			String table = "CREATE TABLE account (" + columns + ");";
			sql += table;
		}
		{
			String columns = "account_id integer REFERENCES account PRIMARY KEY, client_ip VARCHAR(50), master_server_ip VARCHAR(50), game_server_ip VARCHAR(50), authentication_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
			String table = "CREATE TABLE authentication_token (" + columns + ");";
			sql += table;
		}
		{
			String columns = "account_id integer REFERENCES account PRIMARY KEY, last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
			String table = "CREATE TABLE game_server (" + columns + ")";
			sql += table;
		}
		try {
			Statement s = dbc.createStatement();
			s.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IAccountDAO getAccountDAO() {
		return accountDAO;
	}

	@Override
	public IAuthenticationTokenDAO getAuthenticationTokenDAO() {
		return authenticationTokenDAO;
	}

	@Override
	public IGameServerDAO getGameServerDAO() {
		return gameServerDAO;
	}
}
