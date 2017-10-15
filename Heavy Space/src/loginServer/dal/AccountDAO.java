package loginServer.dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import loginServer.dbo.Account;

public class AccountDAO implements IAccountDAO {
	Connection dbc;

	public AccountDAO(Connection dbc) {
		this.dbc = dbc;
	}

	@Override
	public List<Account> getAccounts() {
		try {
			Statement s = dbc.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM ACCOUNT;");
			List<Account> accounts = new ArrayList<Account>();
			while (rs.next()) {
				accounts.add(fillAccount(rs));
			}
			return accounts;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Account getAccount(int id) {
		return null;
	}

	@Override
	public Account getAccount(String username) {
		try {
			Statement s = dbc.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM ACCOUNT;");
			Account account = null;
			while (rs.next()) {
				account = fillAccount(rs);
			}
			return account;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Account fillAccount(ResultSet rs) throws SQLException {
		Account account;
		int id = rs.getInt(Account.ID);
		String username = rs.getString(Account.USERNAME);
		String password = rs.getString(Account.PASSWORD);
		Date createdDate = rs.getDate(Account.CREATED_DATE);
		account = new Account(id, username, password, createdDate);
		return account;
	}

	@Override
	public void updateAccount(int id) {
	}

}
