package loginServer.dal;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

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

	@Override
	public void updateAccount(int id) {
	}

	@Override
	public void createAccount(String username, String password) {
		String salt = BCrypt.gensalt(13);
		String hashedPassword = BCrypt.hashpw(password, salt);
		try {
			Statement s = dbc.createStatement();
			String sql = "INSERT INTO account (" + Account.USERNAME + "," + Account.PASSWORD + ") " + "VALUES ('" + username + "','" + hashedPassword + "');";
			s.executeUpdate(sql);
			dbc.commit();
			System.out.println("NEW USER CREATED");
		} catch (SQLException e) {
			e.printStackTrace();
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
}
