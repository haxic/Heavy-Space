package tests.servers.online;

import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import org.junit.Test;

import shared.Config;
import shared.dbo.GameServerInfo;
import shared.rmi.IAuthenticationServerRMI;
import shared.rmi.IMasterServerRMI;
import tests.LocalConfig;
import tests.dbsetup.DBTestSetup;
import tests.dbsetup.OnlineUserData;

public class GameServerHostAndJoinTester extends DBTestSetup {
	Config localConfig = new LocalConfig();

	@Test
	public void gameServerHostAndJoinTester() {
		try {
			// Connect to RMI
			IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:" + localConfig.authenticationServerPort + "/authenticate");
			// Authenticate and fetch result
			authenticationServerRMI.createAccount(OnlineUserData.USERNAME, OnlineUserData.PASSWORD);
			String result = authenticationServerRMI.authenticate(OnlineUserData.USERNAME, OnlineUserData.PASSWORD);
			// Get master server ip and authentication token from result
			String[] splitResult = result.split("\\s+");
			String masterServerIP = splitResult[0];
			String token = splitResult[1];

			// Host game server through master server
			IMasterServerRMI masterServerRMI = (IMasterServerRMI) Naming.lookup("rmi://" + masterServerIP + "/master");
			masterServerRMI.hostGameServer(token, OnlineUserData.USERNAME);

			List<GameServerInfo> serverList = masterServerRMI.getGameServerList(token, OnlineUserData.USERNAME);
			String ip = masterServerRMI.joinGameServer(token, OnlineUserData.USERNAME, serverList.get(0).getServerIP());
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
			fail();
		}
	}
}
