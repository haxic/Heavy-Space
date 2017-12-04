package shared.systems;

import java.util.List;

import org.joml.Vector3f;

import gameServer.GameServer;
import hecs.Entity;
import hecs.EntityManager;
import hecs.EntitySystem;
import shared.components.AIBotComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.functionality.Globals;

public class AIBotSystem extends EntitySystem {
	public AIBotSystem(EntityManager entityManager) {
		super(entityManager);
	}

	Vector3f tempVector = new Vector3f();

	public void update() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(AIBotComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			AIBotComponent aiBot = (AIBotComponent) entityManager.getComponentInEntity(entity, AIBotComponent.class);
			ObjectComponent unit = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);

			Vector3f angularThrust = new Vector3f(0, 0, 0.025f);

			movement.getAngularAcc().add(angularThrust);

			int distance = 20;

			if (unit.getPosition().z >= distance) {
				aiBot.direction = 1;
			} else if (unit.getPosition().z <= -distance) {
				aiBot.direction = -1;
			}

			float speed = movement.getLinearVel().length();
			if (speed < aiBot.maxSpeed || (movement.getLinearVel().z > 0 && aiBot.direction > 0) || (movement.getLinearVel().z < 0 && aiBot.direction < 0)) {
				Vector3f linearThrust = unit.getForward().mul(aiBot.acceleration * aiBot.direction, tempVector);
				movement.getLinearAcc().add(linearThrust);
			}
		}
	}

}
