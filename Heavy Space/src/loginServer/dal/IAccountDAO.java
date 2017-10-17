package loginServer.dal;

import java.sql.SQLException;
import java.util.List;

import loginServer.dbo.Account;

public interface IAccountDAO {
	public List<Account> getAccounts() throws SQLException;
	public Account getAccount(String username) throws SQLException;
	public void createAccount(String username, String password) throws SQLException;
	public void updateAccountField(int id, String field, Object value) throws SQLException;
}
