package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILoginServerRMI extends Remote {
	public String authenticate(String username, String password) throws RemoteException;
	public void createAccount(String username, String password) throws RemoteException;
}
