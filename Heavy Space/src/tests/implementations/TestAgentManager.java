package tests.implementations;

import gameServer.core.ClientManager;
import gameServer.core.PlayerManager;
import hecs.EntityManager;

public class TestAgentManager extends ClientManager {

	public TestAgentManager(PlayerManager playerManager, EntityManager entityManager) {
		super(entityManager, playerManager);
	}

}
