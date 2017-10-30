package shared.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import shared.dbo.GameServer;
import shared.dbo.GameServerInfo;

public interface IMasterServerRMI extends Remote {
	public List<GameServerInfo> getGameServerList(String token, String username) throws RemoteException;

	public String joinGameServer(String token, String username, String ip) throws RemoteException;

	public String hostGameServer(String token, String username) throws RemoteException;

	public boolean checkClient(String token, String username, String clientToken, String clientUsername) throws RemoteException;
	
	public String heartbeat(String token, String username) throws RemoteException;

}
