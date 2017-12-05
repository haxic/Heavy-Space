package client.gameData;

import java.util.HashMap;
import java.util.Map;

import client.components.ModelComponent;
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
		ModelComponent model = (ModelComponent) entityManager.getComponentInEntity(entity, ModelComponent.class);
		if (model != null)
			entities.remove(model.getEEID());
	}

	public void addEntity(int eeid, Entity entity) {
		entities.put(eeid, entity);
		entity.attach(this);
		entityManager.addComponent(new ModelComponent(eeid), entity);
	}

	public void removeEntity(Entity entity) {
		detach(entity);
	}

}
