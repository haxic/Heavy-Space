package shared.dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import shared.dbo.Account;
import shared.dbo.AuthenticationToken;
import shared.idal.IAccountDAO;

public class AccountDAO implements IAccountDAO {
	private Connection dbc;

	public AccountDAO(Connection dbc) {
		this.dbc = dbc;
	}

	@Override
	public List<Account> getAccounts() throws SQLException {
		Statement s = dbc.createStatement();
		ResultSet rs = s.executeQuery("SELECT * FROM " + Account.ACCOUNT + ";");
		List<Account> accounts = new ArrayList<Account>();
		while (rs.next()) {
			accounts.add(fillAccount(rs));
		}
		return accounts;
	}

	@Override
	public Account getAccount(String username) throws SQLException {
		Statement s = dbc.createStatement();
		ResultSet rs = s.executeQuery("SELECT * FROM " + Account.ACCOUNT + " WHERE " + Account.USERNAME + " = '" + username + "';");
		Account account = fillAccount(rs);
		return account;
	}

	@Override
	public void createAccount(String username, String password) throws SQLException {
		Statement s = dbc.createStatement();
		String sql = "INSERT INTO " + Account.ACCOUNT + " (" + Account.USERNAME + "," + Account.PASSWORD + ") " + "VALUES ('" + username + "','" + password + "');";
		s.executeUpdate(sql);
	}

	@Override
	public void updateAccountField(int id, String field, Object value) throws SQLException {
		if (value instanceof String)
			if (!value.equals("DEFAULT"))
				value = "'" + value + "'";
		Statement s = dbc.createStatement();
		s.executeUpdate("UPDATE " + Account.ACCOUNT + " SET " + field + " = " + value + " WHERE " + Account.ID + " = " + id + ";");
	}

	private Account fillAccount(ResultSet rs) throws SQLException {
		while (rs.next()) {
			int id = rs.getInt(Account.ID);
			String username = rs.getString(Account.USERNAME);
			String password = rs.getString(Account.PASSWORD);
			Timestamp createdDate = rs.getTimestamp(Account.CREATED_DATE);
			Account account = new Account(id, username, password, createdDate != null ? createdDate.toLocalDateTime() : null);
			return account;
		}
		return null;
	}
}
