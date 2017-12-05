package client.systems;

import java.util.List;

import client.gameData.GameModel;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.DeathComponent;

public class DeathSystem {

	private EntityManager entityManager;
	private GameModel gameModel;

	public DeathSystem(EntityManager entityManager, GameModel gameModel) {
		this.entityManager = entityManager;
		this.gameModel = gameModel;
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(DeathComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			entityManager.removeEntity(entity);
			gameModel.removeEntity(entity);
		}
	}

}
