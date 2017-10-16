package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import loginServer.dal.DataAccessLayer;
import loginServer.dbo.Account;

public class LoginServerRMI extends UnicastRemoteObject implements ILoginServerRMI {
	DataAccessLayer dal;

	public LoginServerRMI(int port, DataAccessLayer dal) throws RemoteException {
		super(port);
		this.dal = dal;
	}

	@Override
	public String authenticate(String username, String password) {
		Account account = dal.getAccountDAO().getAccount(username);
		return account.toString();
	}

	@Override
	public void createAccount(String username, String password) throws RemoteException {
		dal.getAccountDAO().createAccount(username, password);;
	}
}
