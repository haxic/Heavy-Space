package loginServer.main;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import rmi.ILoginServerRMI;

public class CreateAccountClientTester {
	public static void main(String[] args) {
		try {
			ILoginServerRMI authentication = (ILoginServerRMI) Naming.lookup("rmi://localhost:5252/authenticate");
			authentication.createAccount("test", "test1234");
			String token = authentication.authenticate("test", "test1234");
			System.out.println(token);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
