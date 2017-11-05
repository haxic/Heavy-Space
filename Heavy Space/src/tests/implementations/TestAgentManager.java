package tests.implementations;

import gameServer.AgentManager;
import gameServer.PlayerManager;
import gameServer.network.SocketHandler;

public class TestAgentManager extends AgentManager {

	public TestAgentManager(PlayerManager playerManager) {
		super(playerManager);
	}

	@Override
	public void handleValidatedConnection(SocketHandler socketHandler, String username, String token) {

	}
}
