package rmi;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Date;

import loginServer.config.Config;
import loginServer.dal.IDataAccessLayer;
import loginServer.dbo.Account;
import security.Security;

public class LoginServerRMI extends UnicastRemoteObject implements ILoginServerRMI {
	IDataAccessLayer dal;

	public LoginServerRMI(int port, IDataAccessLayer dal) throws RemoteException {
		super(port);
		this.dal = dal;
	}

	@Override
	public String authenticate(String username, String password) {
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			String error = "A client tried to authenticate. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}

		// Check password
		if (!Security.checkPassword(password, account.getPassword()))
			return null;

		// Get client ip
		String ip = null;
		try {
			ip = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		if (ip == null)
			return null;

		// Update account authentication token
		try {
			dal.getAuthenticationTokenDAO().updateAuthenticationToken(account.getId(), ip, Config.MASTER_SERVER_IP);
		} catch (SQLException e) {
			String error = "A client tried to authenticate. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}

		// Create authentication token
		String token = createAuthenticationToken(account, ip);
		return Config.MASTER_SERVER_IP + " " + token;
	}

	private String createAuthenticationToken(Account account, String ip) {
		String token = Security.encryptPassword(new Date().getTime() + "//" + account.getUsername() + "//" + ip + "//" + Config.MASTER_SERVER_IP + "//" + Security.AUTHENTICATION_SECRET);
		return token;
	}

	@Override
	public void createAccount(String username, String password) throws RemoteException {
		// TODO: Make sure that this is transactional! The created account must be locked until the token has been created for it.
		// TODO: The account should be removed if the token cannot be created.
		// TODO: Get account id on when creating account.
		String hashedPassword = Security.encryptPassword(password);
		try {
			dal.getAccountDAO().createAccount(username, hashedPassword);
			Account account = dal.getAccountDAO().getAccount(username);
			dal.getAuthenticationTokenDAO().createAuthenticationToken(account.getId(), null, null);
		} catch (SQLException e) {
			String error = "A client tried to create an account. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return;
		}
	}
}
