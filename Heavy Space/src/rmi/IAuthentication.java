package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAuthentication extends Remote {
	public String authenticate(String userName, String password) throws RemoteException;
}
