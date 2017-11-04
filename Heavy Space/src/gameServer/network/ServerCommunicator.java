package gameServer.network;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import shared.rmi.IAuthenticationServerRMI;
import shared.rmi.IMasterServerRMI;

public class ServerCommunicator implements IServerCommunicator {

	private IAuthenticationServerRMI authenticationServerRMI;
	private String authenticationServerAddress;

	private IMasterServerRMI masterServerRMI;
	private String masterServerAddress;

	private String username;
	private String password;
	private String token;

	public ServerCommunicator(String authenticationServerAddress) {
		this.authenticationServerAddress = authenticationServerAddress;
	}

	public IAuthenticationServerRMI getAuthenticationServerRMI() {
		return authenticationServerRMI;
	}

	public IMasterServerRMI getMasterServerRMI() {
		return masterServerRMI;
	}

	@Override
	public boolean authenticate(String username, String password) {
		this.username = username;
		this.password = password;
		String result = null;
		try {
			if (authenticationServerRMI == null)
				authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://" + authenticationServerAddress + "/authenticate");
			result = authenticationServerRMI.authenticate(username, password);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			e.printStackTrace();
			return false;
		}
		if (result == null)
			return false;
		try {
			String[] splitResult = result.split("\\s+");
			masterServerAddress = splitResult[0];
			token = splitResult[1];
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		try {
			masterServerRMI = (IMasterServerRMI) Naming.lookup("rmi://" + masterServerAddress + "/master");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
			return false;
		}
		return token != null && masterServerAddress != null;
	}

	@Override
	public boolean validateClient(String clientToken, String clientUsername) {
		try {
			boolean validated = masterServerRMI.checkClient(token, username, clientToken, clientUsername);
			return validated;
		} catch (RemoteException e) {
			e.printStackTrace();
			try {
				masterServerRMI = (IMasterServerRMI) Naming.lookup("rmi://" + masterServerAddress + "/master");
				return masterServerRMI.checkClient(token, username, clientToken, clientUsername);
			} catch (MalformedURLException | RemoteException | NotBoundException e1) {
				e1.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public boolean createAccount(String username, String password) {
		try {
			if (authenticationServerRMI == null)
				authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://" + authenticationServerAddress + "/authenticate");
			return authenticationServerRMI.createAccount(username, password);
		} catch (MalformedURLException | NotBoundException | RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

}
