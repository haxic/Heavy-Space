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

public class MainMasterServer {
	IDataAccessLayer dal;
	
	public MainMasterServer() throws SQLException {
		dal = new DataAccessLayer();

		MasterServerRMI masterServerRMI;
		try {
			masterServerRMI = new MasterServerRMI(Config.MASTER_SERVER_PORT, dal);
			Registry registry = LocateRegistry.createRegistry(Config.MASTER_SERVER_PORT);
			registry.bind("master", masterServerRMI);
			System.out.println("Master server RMI bound.");
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
		
		
		long timer = System.currentTimeMillis();
		int intervals = 5000;
//		int intervals = 60000;

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

	public static void main(String[] args) {
		try {
			new MainMasterServer();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
