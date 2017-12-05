package gameServer.systems;

import java.util.List;

import gameServer.components.RemoveComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.DeathComponent;

public class RemoveSystem {

	private EntityManager entityManager;

	public RemoveSystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(RemoveComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			entityManager.removeEntity(entity);
		}
	}

}
