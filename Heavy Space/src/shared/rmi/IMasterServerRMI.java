package shared.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import shared.GameServerInfo;

public interface IMasterServerRMI extends Remote {
	public List<GameServerInfo> getGameServerList(String token) throws RemoteException;
	public String joinGameServer(String token, String ip) throws RemoteException;
	public String hostGameServer(String token, String username) throws RemoteException;
}
