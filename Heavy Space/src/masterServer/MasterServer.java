package masterServer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

import shared.Config;
import shared.dal.DataAccessLayer;
import shared.idal.IDataAccessLayer;
import shared.rmi.MasterServerRMI;

public class MasterServer {
	IDataAccessLayer dal;
	Config config;

	public MasterServer(Config config) throws SQLException {
		this.config = config;
		dal = new DataAccessLayer(config);

		MasterServerRMI masterServerRMI;
		try {
			masterServerRMI = new MasterServerRMI(config.masterServerPort, dal);
			Registry registry = LocateRegistry.createRegistry(config.masterServerPort);
			registry.bind("master", masterServerRMI);
			System.out.println("Master server RMI bound on: " + config.masterServerPort);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}

		long timer = System.currentTimeMillis();
//		int intervals = 5000;
		 int intervals = 60000000;

		while (true) {
			try {
				if (System.currentTimeMillis() - timer >= intervals) {
					timer += intervals;
					dal.getGameServerDAO().removeTimedOutGameServers();
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
