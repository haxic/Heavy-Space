package tests.implementations;

import gameServer.ClientManager;
import gameServer.PlayerManager;
import shared.functionality.TCPSocket;

public class TestAgentManager extends ClientManager {

	public TestAgentManager(PlayerManager playerManager) {
		super(playerManager);
	}

	@Override
	public void handleValidatedConnection(TCPSocket socketHandler, String username, String token) {

	}
}
