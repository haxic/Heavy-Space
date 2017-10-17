package loginServer.main;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import loginServer.dal.DataAccessLayer;
import rmi.LoginServerRMI;

public class Main {
	DataAccessLayer dal;
	private static final int PORT = 5252;

	public Main() throws SQLException {
		dal = new DataAccessLayer();

		LoginServerRMI loginServerRMI;
		try {
			loginServerRMI = new LoginServerRMI(PORT, dal);
			Registry registry = LocateRegistry.createRegistry(PORT);
			registry.bind("authenticate", loginServerRMI);
			System.out.println("Login server RMI bound.");
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
		
		
		while (true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		try {
			new Main();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
