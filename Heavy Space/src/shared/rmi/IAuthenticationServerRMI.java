package shared.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAuthenticationServerRMI extends Remote {
	public String authenticate(String username, String password) throws RemoteException;
	public boolean createAccount(String username, String password) throws RemoteException;
}
