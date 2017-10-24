package shared.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import masterServer.MasterServerRequestHandler;
import shared.dbo.GameServer;
import shared.idal.IDataAccessLayer;

public class MasterServerRMI extends UnicastRemoteObject implements IMasterServerRMI {
	MasterServerRequestHandler masterServerRequestHandler;

	public MasterServerRMI(int port, IDataAccessLayer dal) throws RemoteException {
		super(port);
		masterServerRequestHandler = new MasterServerRequestHandler(dal);
	}

	@Override
	public List<GameServerInfo> getGameServerList(String token, String username) throws RemoteException {
		return masterServerRequestHandler.getGameServerListForClient(token, username);
	}

	@Override
	public String joinGameServer(String token, String ip) throws RemoteException {
		return masterServerRequestHandler.joinGameServer(token, ip);
	}

	@Override
	public String hostGameServer(String token, String username) throws RemoteException {
		return masterServerRequestHandler.hostGameServer(token, username);
	}

}
