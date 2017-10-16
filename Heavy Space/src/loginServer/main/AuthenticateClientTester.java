package loginServer.main;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import rmi.ILoginServerRMI;

public class AuthenticateClientTester {
	public static void main(String[] args) {
		try {
			ILoginServerRMI authentication = (ILoginServerRMI) Naming.lookup("rmi://localhost:5252/authenticate");
			String token = authentication.authenticate("haxic", "hejsan123");
			System.out.println(token);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
