package shared.systems;

import java.util.List;

import org.joml.Vector3f;

import hecs.Entity;
import hecs.EntityManager;
import shared.components.AIBotComponent;
import shared.components.CollisionComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;

public class MovementSystem {

	private EntityManager entityManager;

	public MovementSystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void process(float dt) {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(MovementComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			// SpawnComponent spawn = (SpawnComponent) entityManager.getComponentInEntity(entity, SpawnComponent.class);
			// if (spawn != null)
			// continue;

			processEntity(dt, entity);
		}
	}

	public void processEntity(float dt, Entity entity) {
		ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
		MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
		CollisionComponent collision = (CollisionComponent) entityManager.getComponentInEntity(entity, CollisionComponent.class);

		movement.getAngularVel().fma(dt, movement.getAngularAcc());
		movement.getAngularAcc().zero();

		object.rotate(dt * movement.getAngularVel().x, dt * movement.getAngularVel().y, dt * movement.getAngularVel().z);

		movement.getLinearVel().fma(dt, movement.getLinearAcc());
		movement.getLinearAcc().zero();

		if (collision != null)
			collision.getPreviousPosition().set(object.getPosition());
		object.getPosition().fma(dt, movement.getLinearVel());
//		System.out.println(dt + " " + object.getPosition().x + " " +object.getPosition().y + " "+ object.getPosition().z);
//		System.out.println(dt + " " + movement.getLinearVel().x + " " +movement.getLinearVel().y + " "+ movement.getLinearVel().z);
//		
//		ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);
//		if (projectile != null)
//			System.out.println("MOVEMENT SYSTEM: " + entity.getEID() + " (" + object.getPosition().x + ":" + object.getPosition().y + ":" + object.getPosition().z + ")" + " ("
//					+ movement.getLinearVel().x + ":" + movement.getLinearVel().y + ":" + movement.getLinearVel().z + ")" + " " + dt);
	}

}
