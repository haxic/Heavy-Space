package tests.online;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import shared.Config;
import shared.rmi.IAuthenticationServerRMI;

public class AuthenticateClientTester {
	public static void main(String[] args) {
		try {
			IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:" + Config.AUTHENTICATION_SERVER_PORT + "/authenticate");
			authenticationServerRMI.createAccount("test", "test123");
			String token = authenticationServerRMI.authenticate("test", "test123");
			System.out.println(token);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
