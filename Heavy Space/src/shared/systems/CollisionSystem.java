package shared.systems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Vector3f;

import hecs.Entity;
import hecs.EntityManager;
import shared.components.CollisionComponent;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.functionality.BoundingBox;

public class CollisionSystem {

	private EntityManager entityManager;

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
		System.out.println("UUUUUUUUUUPPPPPPPPDDDDDDAAAAAAAAAAATTTTTTTTTTEEEEEEEEEEEEEEEEEEEEEEEE");
		System.out.println("UUUUUUUUUUPPPPPPPPDDDDDDAAAAAAAAAAATTTTTTTTTTEEEEEEEEEEEEEEEEEEEEEEEE");
		System.out.println("UUUUUUUUUUPPPPPPPPDDDDDDAAAAAAAAAAATTTTTTTTTTEEEEEEEEEEEEEEEEEEEEEEEE");
		for (Entity entity : entities) {
			CollisionComponent collision = (CollisionComponent) entityManager.getComponentInEntity(entity, CollisionComponent.class);
			ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			updateBoundingBox(collision.getPreviousPosition(), object.getPosition(), collision.getRadius(), collision.getBoundingBox());
			System.out.println("EURUEKAAA!!! " + entity.getEID()
			+ " (" + (int) collision.getPreviousPosition().x + ":" + (int) collision.getPreviousPosition().y + ":" + (int) collision.getPreviousPosition().z + ") " 
					+ " (" + (int) object.getPosition().x + ":" + (int) object.getPosition().y + ":" + (int) object.getPosition().z + ") "
			+ collision.getBoundingBox().toStringAsInts());
	
		}

		System.out.println("SAAAAAAAAAAAAAAAAAAAAAAAAAAAAATAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAN");
		System.out.println("SAAAAAAAAAAAAAAAAAAAAAAAAAAAAATAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAN");
		System.out.println("SAAAAAAAAAAAAAAAAAAAAAAAAAAAAATAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAN");
		for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			iterator.remove();
			entities.remove(entity);

			CollisionComponent collision = (CollisionComponent) entityManager.getComponentInEntity(entity, CollisionComponent.class);
			ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);
			System.out.println(entity.getEID());
			for (Entity checkedEntity : entities) {
				CollisionComponent checkedCollision = (CollisionComponent) entityManager.getComponentInEntity(checkedEntity, CollisionComponent.class);
				ObjectComponent checkedObject = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
				ProjectileComponent checkedProjectile = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);
				System.out.println("         " + checkedEntity.getEID());

				if (projectile != null && projectile.getShipEntity().equals(checkedEntity) || checkedProjectile != null && checkedProjectile.getShipEntity().equals(entity)) {
					System.out.println("IGNORE IF PROJECTILE TARGETS OWNER");
				}

				if (checkCollision(collision.getBoundingBox(), checkedCollision.getBoundingBox())) {
					System.out.println("EURUEKAAA!!! " + entity.getEID() + " (" + (int) object.getPosition().x + ":" + (int) object.getPosition().y + ":" + (int) object.getPosition().z + ") " + collision.getBoundingBox().toStringAsInts()
					+ "    |    " + 
							checkedEntity.getEID() + " (" + (int) object.getPosition().x + ":" + (int) object.getPosition().y + ":"	+ (int) object.getPosition().z + ") " + checkedCollision.getBoundingBox().toStringAsInts());
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
