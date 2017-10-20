package authenticationServer.tests;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import shared.rmi.IAuthenticationServerRMI;

public class AuthenticateClientTester {
	public static void main(String[] args) {
		try {
			
			IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:5252/authenticate");
			authenticationServerRMI.createAccount("test", "test123");
			String token = authenticationServerRMI.authenticate("test", "test123");
			System.out.println(token);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
