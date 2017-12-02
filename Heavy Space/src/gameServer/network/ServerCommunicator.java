package gameServer.network;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import gameServer.ServerConfig;
import shared.rmi.IAuthenticationServerRMI;
import shared.rmi.IMasterServerRMI;

public class ServerCommunicator implements IServerCommunicator {

	private IAuthenticationServerRMI authenticationServerRMI;

	private IMasterServerRMI masterServerRMI;

	private String username;
	private String password;
	private String token;

	private ServerConfig serverConfig;

	public ServerCommunicator(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
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
			// TODO: include port
			if (authenticationServerRMI == null)
				authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://" + serverConfig.authenticationServerIP.getHostAddress() + "/authenticate");
			result = authenticationServerRMI.authenticate(username, password);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			e.printStackTrace();
			return false;
		}
		if (result == null)
			return false;
		try {
			String[] splitResult = result.split("\\s+");
			serverConfig.masterServerIP = InetAddress.getByName(splitResult[0]);
			serverConfig.authenticationServerPort = Integer.parseInt(splitResult[1]);
			token = splitResult[2];
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		try {
			// TODO: include port
			masterServerRMI = (IMasterServerRMI) Naming.lookup("rmi://" + serverConfig.masterServerIP.getHostAddress() + "/master");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
			return false;
		}
		return token != null;
	}

	@Override
	public boolean validateClient(String clientToken, String clientUsername) {
		try {
			boolean validated = masterServerRMI.checkClient(token, username, clientToken, clientUsername);
			return validated;
		} catch (RemoteException e) {
			e.printStackTrace();
			try {
				// TODO: include port
				masterServerRMI = (IMasterServerRMI) Naming.lookup("rmi://" + serverConfig.masterServerIP.getHostAddress() + "/master");
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
			// TODO: include port
			if (authenticationServerRMI == null)
				authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://" + serverConfig.authenticationServerIP.getHostAddress() + "/authenticate");
			return authenticationServerRMI.createAccount(username, password);
		} catch (MalformedURLException | NotBoundException | RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

}
