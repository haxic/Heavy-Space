package loginServer.main;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.List;

import loginServer.dal.DataAccessLayer;
import loginServer.dbo.Account;
import rmi.Authentication;
import java.security.SecureRandom;

public class Main {
	DataAccessLayer dal;
	private static final int PORT = 5252;

	public Main() throws SQLException {
		dal = new DataAccessLayer();
		SecureRandom random = new SecureRandom();
		byte[] values = new byte[64];
		random.nextBytes(values);
		System.out.println(values.toString());
		List<Account> accounts = dal.getAccountDAO().getAccounts();
		for (Account account : accounts) {
			System.out.println(account);
		}

		Authentication authentication;
		try {

			authentication = new Authentication(PORT, dal);
			Registry registry = LocateRegistry.createRegistry(PORT);
			registry.bind("authenticate", authentication);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				Thread.sleep(50);
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
