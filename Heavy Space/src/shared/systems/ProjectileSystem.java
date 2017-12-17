package shared.systems;

import java.util.ArrayList;
import java.util.List;

import hecs.Entity;
import hecs.EntityManager;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;

public class ProjectileSystem {

	private EntityManager entityManager;

	public ProjectileSystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void process(float dt) {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(ProjectileComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		List<Entity> removedProjectiles = new ArrayList<>();
		for (Entity entity : entities) {
			ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);
			SpawnComponent spawn = (SpawnComponent) entityManager.getComponentInEntity(entity, SpawnComponent.class);

			if (spawn != null)
				continue;
			projectile.update(dt);
			if (projectile.hasElapsed())
				removedProjectiles.add(entity);
		}
		for (Entity entity : removedProjectiles) {
			entityManager.removeEntity(entity);
		}
	}

}
