package authenticationServer.tests;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import shared.rmi.IAuthenticationServerRMI;

public class CreateAccountClientTester {
	public CreateAccountClientTester() {

		for (int i = 50; i < 100; i++) {
			new Worker(i).start();

		}

	}

	public static void main(String[] args) {
		new CreateAccountClientTester();
	}

	class Worker extends Thread {
		int i;

		public Worker(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			try {
				IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:5252/authenticate");
				authenticationServerRMI.createAccount("test" + i, "test" + (i + i));
				String token = authenticationServerRMI.authenticate("test" + i, "test" + (i + i));
				System.out.println(token);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}

	}
}
