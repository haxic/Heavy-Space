package loginServer.main;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import rmi.ILoginServerRMI;

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
				ILoginServerRMI authentication = (ILoginServerRMI) Naming.lookup("rmi://localhost:5252/authenticate");
				authentication.createAccount("test" + i, "test" + (i + i));
				String token = authentication.authenticate("test" + i, "test" + (i + i));
				System.out.println(token);
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}

	}
}
