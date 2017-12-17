package tests.implementations;

import gameServer.core.ClientManager;
import gameServer.core.PlayerManager;
import hecs.EntityManager;

public class TestClientManager extends ClientManager {

	public TestClientManager(PlayerManager playerManager, EntityManager entityManager) {
		super(entityManager, playerManager);
	}

}
