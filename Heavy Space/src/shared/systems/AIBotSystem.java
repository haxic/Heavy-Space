package shared.systems;

import java.util.List;

import org.joml.Vector3f;

import gameServer.components.ShipComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.AIBotComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;

public class AIBotSystem {
	private EntityManager entityManager;

	public AIBotSystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	Vector3f tempVector = new Vector3f();

	public void update(float dt) {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(AIBotComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			AIBotComponent aiBot = (AIBotComponent) entityManager.getComponentInEntity(entity, AIBotComponent.class);
			ShipComponent ship = (ShipComponent) entityManager.getComponentInEntity(entity, ShipComponent.class);
			ObjectComponent unit = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);

			Vector3f angularThrust = new Vector3f(0.01f, 0.02f, 0.03f);

			movement.getAngularAcc().fma(dt, angularThrust);

			int distance = 100;

			if (unit.getPosition().z >= distance) {
				aiBot.direction = 1;
			} else if (unit.getPosition().z <= -distance) {
				aiBot.direction = -1;
			}

			float speed = movement.getLinearVel().length();
			if (speed < aiBot.maxSpeed || (movement.getLinearVel().z > 0 && aiBot.direction > 0) || (movement.getLinearVel().z < 0 && aiBot.direction < 0)) {
				Vector3f linearThrust = unit.getForward().mul(aiBot.acceleration * aiBot.direction, tempVector);
				ship.getLinearThrust().add(linearThrust);
			}
		}
	}

}
