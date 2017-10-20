package shared.rmi;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import security.Authenticater;
import shared.Config;
import shared.GameServerInfo;
import shared.dal.IDataAccessLayer;
import shared.dbo.Account;

public class MasterServerRMI extends UnicastRemoteObject implements IMasterServerRMI {
	MasterServerRequestHandler masterServerRequestHandler;

	public MasterServerRMI(int port, IDataAccessLayer dal) throws RemoteException {
		super(port);
		masterServerRequestHandler = new MasterServerRequestHandler(dal);
	}

	@Override
	public List<GameServerInfo> getGameServerList(String token) throws RemoteException {
		return masterServerRequestHandler.getGameServerList(token);
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
