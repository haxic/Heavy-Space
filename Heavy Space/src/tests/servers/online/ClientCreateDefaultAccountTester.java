package tests.servers.online;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import shared.Config;
import shared.rmi.IAuthenticationServerRMI;
import tests.dbsetup.OnlineUserData;

public class ClientCreateDefaultAccountTester {
	public static void main(String[] args) {
		try {
			// Connect to RMI
			IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:" + Config.AUTHENTICATION_SERVER_PORT + "/authenticate");
			// Create account
			authenticationServerRMI.createAccount(OnlineUserData.USERNAME, OnlineUserData.PASSWORD);
			System.out.println("Account created \"" + OnlineUserData.USERNAME + "\" with password \"" + OnlineUserData.PASSWORD + "\"");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
