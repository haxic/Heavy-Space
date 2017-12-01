package client.main;

import java.util.HashMap;
import java.util.Map;

import hecs.Entity;
import hecs.EntityContainer;
import hecs.EntityManager;

public class GameModel implements EntityContainer {
	EntityManager entityManager;
	Map<Integer, Entity> entities;

	public GameModel(EntityManager entityManager) {
		this.entityManager = entityManager;
		entities = new HashMap<>();
	}

	public Entity getEntity(int eeid) {
		return entities.get(eeid);
	}

	@Override
	public void detach(Entity entity) {
	}

	public void addEntity(int eeid, Entity entity) {
		entities.put(eeid, entity);
		entity.attach(this);
	}

}
