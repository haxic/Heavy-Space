package authenticationServer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.List;

import shared.Config;
import shared.dal.DataAccessLayer;
import shared.dbo.GameServer;
import shared.idal.IDataAccessLayer;
import shared.rmi.AuthenticationServerRMI;

public class MainAuthenticationServer {
	IDataAccessLayer dal;

	public MainAuthenticationServer() throws SQLException {
		dal = new DataAccessLayer();

		AuthenticationServerRMI authenticationServerRMI;
		try {
			authenticationServerRMI = new AuthenticationServerRMI(Config.AUTHENTICATION_SERVER_PORT, dal);
			Registry registry = LocateRegistry.createRegistry(Config.AUTHENTICATION_SERVER_PORT);
			registry.bind("authenticate", authenticationServerRMI);
			System.out.println("Login server RMI bound.");
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}


		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		try {
			new MainAuthenticationServer();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
