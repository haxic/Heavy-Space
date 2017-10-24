package tests.online;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import shared.Config;
import shared.rmi.IAuthenticationServerRMI;
import shared.rmi.IMasterServerRMI;

public class HostGameServerTest {
	public static void main(String[] args) {
		try {
			String username = "test";
			String password = "test123";
			String token;
			IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:" + Config.AUTHENTICATION_SERVER_PORT + "/authenticate");
			authenticationServerRMI.createAccount("test", "test123");
			token = authenticationServerRMI.authenticate("test", "test123");
			System.out.println(token);
			
			IMasterServerRMI masterServerRMI = (IMasterServerRMI) Naming.lookup("rmi://localhost:" + Config.MASTER_SERVER_PORT + "/master");
			masterServerRMI.hostGameServer(token, "test");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
