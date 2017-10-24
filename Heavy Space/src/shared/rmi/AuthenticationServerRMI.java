package shared.rmi;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Date;

import authenticationServer.AuthenticationRequestHandler;
import security.Authenticater;
import shared.Config;
import shared.dbo.Account;
import shared.dbo.AuthenticationToken;
import shared.idal.IDataAccessLayer;

public class AuthenticationServerRMI extends UnicastRemoteObject implements IAuthenticationServerRMI {
	AuthenticationRequestHandler authenticationRequestHandler;

	public AuthenticationServerRMI(int port, IDataAccessLayer dal) throws RemoteException {
		super(port);
		authenticationRequestHandler = new AuthenticationRequestHandler(dal);
	}

	@Override
	public String authenticate(String username, String password) {
		// Get client ip
		String ip = null;
		try {
			ip = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		if (ip == null)
			return null;
		return authenticationRequestHandler.authenticate(username, password, ip);
	}

	@Override
	public void createAccount(String username, String password) throws RemoteException {
		// Get client ip
		String ip = null;
		try {
			ip = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		if (ip == null)
			return;
		authenticationRequestHandler.createAccount(username, password, ip);
	}
}
