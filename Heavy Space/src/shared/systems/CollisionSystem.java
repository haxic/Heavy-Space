package shared.systems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Vector3f;

import hecs.Entity;
import hecs.EntityManager;
import shared.components.CollisionComponent;
import shared.components.DeathComponent;
import shared.components.HealthComponent;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.functionality.BoundingBox;

public class CollisionSystem {

	private EntityManager entityManager;
	private Vector3f tempVector1 = new Vector3f();
	private Vector3f tempVector2 = new Vector3f();
	private Vector3f tempVector3 = new Vector3f();
	private Vector3f tempVector4 = new Vector3f();

	public CollisionSystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(CollisionComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		List<Entity> removedProjectiles = new ArrayList<>();
		for (Entity entity : entities) {
			CollisionComponent collision = (CollisionComponent) entityManager.getComponentInEntity(entity, CollisionComponent.class);
			ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			updateBoundingBox(collision.getPreviousPosition(), object.getPosition(), collision.getRadius(), collision.getBoundingBox());

		}

		for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			iterator.remove();
			entities.remove(entity);

			CollisionComponent collision = (CollisionComponent) entityManager.getComponentInEntity(entity, CollisionComponent.class);
			ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);

//			System.out.println("COLLISION FOR SOME REASON Oo");
			for (Entity checkedEntity : entities) {
				CollisionComponent checkedCollision = (CollisionComponent) entityManager.getComponentInEntity(checkedEntity, CollisionComponent.class);
				ObjectComponent checkedObject = (ObjectComponent) entityManager.getComponentInEntity(checkedEntity, ObjectComponent.class);
				ProjectileComponent checkedProjectile = (ProjectileComponent) entityManager.getComponentInEntity(checkedEntity, ProjectileComponent.class);

				if (projectile != null && checkedProjectile != null)
					continue;
				if (projectile != null && projectile.getShipEntity() != null && projectile.getShipEntity().equals(checkedEntity))
					continue;
				if (checkedProjectile != null && checkedProjectile.getShipEntity() != null && checkedProjectile.getShipEntity().equals(entity))
					continue;

				if (checkCollision(collision.getBoundingBox(), checkedCollision.getBoundingBox())) {
					float distanceSquaredA2B2 = object.getPosition().sub(checkedObject.getPosition(), tempVector1).lengthSquared();
					float distanceSquaredA1B1 = collision.getPreviousPosition().sub(checkedCollision.getPreviousPosition(), tempVector2).lengthSquared();
					float distanceSquaredA2B1 = object.getPosition().sub(checkedCollision.getPreviousPosition(), tempVector3).lengthSquared();
					float distanceSquaredA1B2 = collision.getPreviousPosition().sub(checkedObject.getPosition(), tempVector4).lengthSquared();

					float radiusSumSquared = (collision.getRadius() + checkedCollision.getRadius()) * (collision.getRadius() + checkedCollision.getRadius());
					if (distanceSquaredA2B2 <= radiusSumSquared || distanceSquaredA1B1 <= radiusSumSquared || distanceSquaredA2B1 <= radiusSumSquared || distanceSquaredA1B2 <= radiusSumSquared) {
						HealthComponent health = (HealthComponent) entityManager.getComponentInEntity(entity, HealthComponent.class);
						HealthComponent checkedHealth = (HealthComponent) entityManager.getComponentInEntity(checkedEntity, HealthComponent.class);

						DeathComponent death = (DeathComponent) entityManager.getComponentInEntity(entity, DeathComponent.class);
						DeathComponent checkedDeath = (DeathComponent) entityManager.getComponentInEntity(checkedEntity, DeathComponent.class);
					
						
						
//						System.out.println(
//								entity.getEID() + " (" + object.getPosition().x + ":" + object.getPosition().y + ":" + object.getPosition().z + ") " + collision.getBoundingBox().toStringAsInts());
//						System.out.println(checkedEntity.getEID() + " (" + checkedObject.getPosition().x + ":" + checkedObject.getPosition().y + ":" + checkedObject.getPosition().z + ") "
//								+ checkedCollision.getBoundingBox().toStringAsInts());
					
						
						
						// Ships / Structures / Obstacles etc -> HealthComponent
						// Projectiles -> ProjectileComponent
						// Indestructible objects -> non of the above
						if (death == null) {
							if (health != null) {
								if (checkedProjectile != null) {
									health.setCoreIntegrity(health.getCoreIntegrity() - checkedProjectile.getDamage());
									if (health.getCoreIntegrity() <= 0) {
										entityManager.addComponent(new DeathComponent(checkedEntity), entity);
									}
								} else {
									health.setCoreIntegrity(0);
									entityManager.addComponent(new DeathComponent(), entity);
								}
							} else if (projectile != null) {
								entityManager.addComponent(new DeathComponent(), entity);
							}
						}

						if (checkedDeath == null) {
							if (checkedHealth != null) {
								if (projectile != null) {
									checkedHealth.setCoreIntegrity(checkedHealth.getCoreIntegrity() - projectile.getDamage());
									if (checkedHealth.getCoreIntegrity() <= 0) {
										entityManager.addComponent(new DeathComponent(entity), checkedEntity);
									}
								} else {
									checkedHealth.setCoreIntegrity(0);
									entityManager.addComponent(new DeathComponent(), checkedEntity);
								}
							} else if (checkedProjectile != null) {
								entityManager.addComponent(new DeathComponent(), checkedEntity);
							}
						}
					}
				}
			}
		}

		for (Entity entity : entities) {
			ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);
			SpawnComponent spawn = (SpawnComponent) entityManager.getComponentInEntity(entity, SpawnComponent.class);
			if (spawn != null)
				continue;
			if (projectile.hasElapsed())
				removedProjectiles.add(entity);
		}

		for (Entity entity : removedProjectiles) {
			entityManager.removeEntity(entity);
		}
	}

	private boolean checkCollision(BoundingBox bb1, BoundingBox bb2) {
		return bb1.xMin < bb2.xMax && bb1.xMax > bb2.xMin && bb1.yMin < bb2.yMax && bb1.yMax > bb2.yMin && bb1.zMin < bb2.zMax && bb1.zMax > bb2.zMin;
	}

	private void updateBoundingBox(Vector3f p1, Vector3f p2, float radius, BoundingBox boundingBox) {
		boundingBox.xMin = (p1.x < p2.x ? p1.x : p2.x) - radius;
		boundingBox.xMax = (p1.x > p2.x ? p1.x : p2.x) + radius;

		boundingBox.yMin = (p1.y < p2.y ? p1.y : p2.y) - radius;
		boundingBox.yMax = (p1.y > p2.y ? p1.y : p2.y) + radius;

		boundingBox.zMin = (p1.z < p2.z ? p1.z : p2.z) - radius;
		boundingBox.zMax = (p1.z > p2.z ? p1.z : p2.z) + radius;
	}

}
