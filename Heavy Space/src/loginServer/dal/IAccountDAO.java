package loginServer.dal;

import java.util.List;

import loginServer.dbo.Account;

public interface IAccountDAO {
	public List<Account> getAccounts();
	public Account getAccount(int id);
	public Account getAccount(String username);
	public void updateAccount(int id);
}
