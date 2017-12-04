package shared.systems;

import java.util.List;

import org.joml.Vector3f;

import hecs.Entity;
import hecs.EntityManager;
import shared.components.AIBotComponent;
import shared.components.CollisionComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.SpawnComponent;
import shared.functionality.Globals;

public class MovementSystem {

	private EntityManager entityManager;

	public MovementSystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(MovementComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			SpawnComponent spawn = (SpawnComponent) entityManager.getComponentInEntity(entity, SpawnComponent.class);
			if (spawn != null)
				continue;

			ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
			CollisionComponent collision = (CollisionComponent) entityManager.getComponentInEntity(entity, CollisionComponent.class);
			movement.getAngularVel().fma(Globals.dt, movement.getAngularAcc());
			movement.getAngularAcc().zero();
			object.pitch(Globals.dt * movement.getAngularVel().x);
			object.yaw(Globals.dt * movement.getAngularVel().y);
			object.roll(Globals.dt * movement.getAngularVel().z);

			movement.getLinearVel().fma(Globals.dt, movement.getLinearAcc());
			movement.getLinearAcc().zero();

			if (collision != null)
				collision.getPreviousPosition().set(object.getPosition());
			object.getPosition().fma(Globals.dt, movement.getLinearVel());
		}
	}

}
