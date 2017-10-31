package tests.servers.online;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import shared.Config;
import shared.rmi.IAuthenticationServerRMI;

public class ClientCreateMultipleAccountsTester {
	public ClientCreateMultipleAccountsTester() {
		// Spawn multiple threads that execute at the same time
		for (int i = 50; i < 100; i++)
			new Worker(i).start();
	}

	class Worker extends Thread {
		int i;

		public Worker(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			try {
				// Connect to RMI
				IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:" + Config.AUTHENTICATION_SERVER_PORT + "/authenticate");
				// Create account
				authenticationServerRMI.createAccount("test" + i, "test" + (i + i));
				// Authenticate and fetch result
				String token = authenticationServerRMI.authenticate("test" + i, "test" + (i + i));
				System.out.println(token);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new ClientCreateMultipleAccountsTester();
	}
}
