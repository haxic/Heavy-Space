package client.systems;

import java.util.List;

import client.main.Scene;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.functionality.Globals;

public class SpawnSystem {

	private EntityManager entityManager;
	private Scene scene;

	public SpawnSystem(EntityManager entityManager, Scene scene) {
		this.entityManager = entityManager;
		this.scene = scene;
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(SpawnComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			SpawnComponent spawnComponent = (SpawnComponent) entityManager.getComponentInEntity(entity, SpawnComponent.class);
			if (Globals.tick >= spawnComponent.getTick()) {
				ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);
				if (projectile != null) {
					ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
					projectile.activate();
				}
				scene.addEntity(entity);
				entityManager.removeComponentAll(SpawnComponent.class, entity);
			}
		}
	}
}
