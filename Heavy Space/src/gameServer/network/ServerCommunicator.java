package gameServer.network;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import gameServer.core.ServerConfig;
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

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getToken() {
		return token;
	}

	@Override
	public boolean authenticate(String username, String password) {
		this.username = username;
		this.password = password;
		String result = null;
		try {
			if (authenticationServerRMI == null)
				connectAuthenticationServer();
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
			serverConfig.masterServerPort = Integer.parseInt(splitResult[1]);
			token = splitResult[2];
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return token != null;
	}

	@Override
	public boolean validateClient(String clientToken, String clientUsername) {
		try {
			System.out.println(token + " " + username + " " + clientToken + " " + clientUsername);
			connectMasterServer();
			boolean validated = masterServerRMI.checkClient(token, username, clientToken, clientUsername);
			return validated;
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean createAccount(String username, String password) {
		try {
			// TODO: include port
			connectAuthenticationServer();
			boolean successful = authenticationServerRMI.createAccount(username, password);
			return successful;
		} catch (MalformedURLException | NotBoundException | RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void connectAuthenticationServer() throws NotBoundException, MalformedURLException, RemoteException {
		if (authenticationServerRMI == null)
			authenticationServerRMI = (IAuthenticationServerRMI) Naming
					.lookup("rmi://" + serverConfig.authenticationServerIP.getHostAddress() + ":" + serverConfig.authenticationServerPort + "/authenticate");
	}

	private void connectMasterServer() throws MalformedURLException, RemoteException, NotBoundException {
		if (masterServerRMI == null)
			masterServerRMI = (IMasterServerRMI) Naming.lookup("rmi://" + serverConfig.masterServerIP.getHostAddress() + ":" + serverConfig.masterServerPort + "/master");
	}

}
