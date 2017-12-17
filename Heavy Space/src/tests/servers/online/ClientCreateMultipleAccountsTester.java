package tests.servers.online;

import static org.junit.Assert.assertEquals;
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

public class ClientCreateMultipleAccountsTester extends DBTestSetup {
	Config localConfig = new LocalConfig();
	int readyCounter;
	IAuthenticationServerRMI authenticationServerRMI;

	@Test
	public void clientCreateMultipleAccountsTester() {
		// Spawn multiple threads that execute at the same time
		try {
			authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:" + localConfig.authenticationServerPort + "/authenticate");
		} catch (MalformedURLException | RemoteException | NotBoundException e1) {
			e1.printStackTrace();
			fail();
		}
		for (int i = 0; i < 40; i++)
			new Worker(i).start();
		while (readyCounter < 40) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			}
		}
	}

	class Worker extends Thread implements Runnable {
		int i;
		String username;
		String password;

		public Worker(int i) {
			this.i = i;
			username = "test" + i;
			password = "test" + (i + i);
		}

		@Override
		public void run() {
			// Connect to RMI
			// Create account
			try {
				authenticationServerRMI.createAccount(username, password);
				// Authenticate and fetch result
				String result = authenticationServerRMI.authenticate(username, password);
				String[] splitResult = result.split("\\s+");
				String ip = splitResult[0];
				String port = splitResult[1];
				String token = splitResult[2];
				String resultUsername = splitResult[3];
				assertEquals(username, resultUsername);
				assertTrue(token != null);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail();
			}
			readyCounter++;
		}
	}
}
