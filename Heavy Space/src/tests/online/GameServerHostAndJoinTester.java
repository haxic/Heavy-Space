package tests.online;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import shared.Config;
import shared.dbo.GameServerInfo;
import shared.rmi.IAuthenticationServerRMI;
import shared.rmi.IMasterServerRMI;
import tests.dbsetup.OnlineUserData;

public class GameServerHostAndJoinTester {
	public static void main(String[] args) {
		try {
			// Connect to RMI
			IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:" + Config.AUTHENTICATION_SERVER_PORT + "/authenticate");
			// Authenticate and fetch result
			String result = authenticationServerRMI.authenticate(OnlineUserData.USERNAME, OnlineUserData.PASSWORD);
			// Get master server ip and authentication token from result
			String[] splitResult = result.split("\\s+");
			String masterServerIP = splitResult[0];
			String token = splitResult[1];
			
			// Host game server through master server
			IMasterServerRMI masterServerRMI = (IMasterServerRMI) Naming.lookup("rmi://" + masterServerIP + ":" + Config.MASTER_SERVER_PORT + "/master");
			masterServerRMI.hostGameServer(token, OnlineUserData.USERNAME);
			
			List<GameServerInfo> serverList = masterServerRMI.getGameServerList(token, OnlineUserData.USERNAME);
			masterServerRMI.joinGameServer(token, OnlineUserData.USERNAME, serverList.get(0).getServerIP());
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
