package authenticationServer;

import java.sql.SQLException;

import security.Authenticater;
import shared.Config;
import shared.dbo.Account;
import shared.dbo.AuthenticationToken;
import shared.idal.IDataAccessLayer;

public class AuthenticationRequestHandler {
	private IDataAccessLayer dal;
	private Config config;

	public AuthenticationRequestHandler(IDataAccessLayer dal, Config config) {
		this.dal = dal;
		this.config = config;
	}

	public String authenticate(String username, String password, String ip) {
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		// Check that an account was retrieved
		if (account == null)
			return null;

		// Check password
		if (!Authenticater.checkPassword(password, account.getPassword()))
			return null;

		// Update account authentication token
		AuthenticationToken authenticationToken = null;
		try {
			dal.getAuthenticationTokenDAO().updateAuthenticationToken(account.getID(), ip, config.masterServerIP + ":" + config.masterServerPort);
			authenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		// Create authentication token
		String token = Authenticater.getAuthenticationTokenAsHashedString(authenticationToken);
		return config.masterServerIP + ":" + config.masterServerPort + " " + token + " " + username;
	}

	public boolean createAccount(String username, String password, String ip) {
		// TODO: Make sure that this is transactional! The created account must
		// be locked until the token has been created for it.
		// TODO: The account should be removed if the token cannot be created.
		// TODO: Get account id on when creating account.
		String hashedPassword = Authenticater.encryptPassword(password);
		try {
			dal.getAccountDAO().createAccount(username, hashedPassword);
			Account account = dal.getAccountDAO().getAccount(username);
			dal.getAuthenticationTokenDAO().createAuthenticationToken(account.getID(), ip, config.masterServerIP + ":" + config.masterServerPort);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
