package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import loginServer.dal.DataAccessLayer;
import loginServer.dbo.Account;

public class Authentication extends UnicastRemoteObject implements IAuthentication {
	DataAccessLayer dal;

	public Authentication(int port, DataAccessLayer dal) throws RemoteException {
		super(port);
		this.dal = dal;
	}

	@Override
	public String authenticate(String username, String password) {
		Account account = dal.getAccountDAO().getAccount(username);
		return account.toString();
	}
}
