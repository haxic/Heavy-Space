package tests.servers.online;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.Test;

import shared.Config;
import shared.rmi.IAuthenticationServerRMI;
import tests.LocalConfig;
import tests.dbsetup.DBTestSetup;
import tests.dbsetup.OnlineUserData;

public class ClientCreateDefaultAccountTester extends DBTestSetup {
	Config localConfig = new LocalConfig();

	@Test
	public void clientCreateDefaultAccountTester() {
		try {
			// Connect to RMI
			IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming
					.lookup("rmi://" + localConfig.authenticationServerIP + ":" + localConfig.authenticationServerPort + "/authenticate");
			// Create account
			boolean created = authenticationServerRMI.createAccount(OnlineUserData.USERNAME, OnlineUserData.PASSWORD);
			assertTrue(created);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
			fail();
		}
	}
}
