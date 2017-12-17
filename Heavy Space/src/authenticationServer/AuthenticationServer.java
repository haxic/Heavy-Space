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
import tests.LocalConfig;

public class AuthenticationServer {
	IDataAccessLayer dal;
	Config config;

	public AuthenticationServer(Config config) throws SQLException {
		this.config = config;
		dal = new DataAccessLayer(config);
		AuthenticationServerRMI authenticationServerRMI;
		try {
			authenticationServerRMI = new AuthenticationServerRMI(config.authenticationServerPort, dal, config);
//			Registry registry = LocateRegistry.createRegistry(config.authenticationServerPort);
			Registry registry = LocateRegistry.createRegistry(config.authenticationServerPort);
			registry.bind("authenticate", authenticationServerRMI);
			System.out.println("Authentication server RMI bound on: " + config.authenticationServerPort + " " + registry);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
}
