package tests.implementations;

import gameServer.network.IServerCommunicator;

public class ServerCommunicatorLocal implements IServerCommunicator {

	@Override
	public boolean authenticate(String username, String password) {
		return true;
	}

	@Override
	public boolean validateClient(String clientToken, String clientUsername) {
		return true;
	}

	@Override
	public boolean createAccount(String username, String password) {
		return true;
	}

}
