package gameServer.systems;

import java.util.List;

import org.joml.Vector3f;

import gameServer.GameServer;
import gameServer.components.AIBotComponent;
import hecs.Entity;
import hecs.EntityManager;
import hecs.EntitySystem;
import shared.components.MovementComponent;
import shared.components.UnitComponent;

public class AIBotSystem extends EntitySystem {
	public AIBotSystem(EntityManager entityManager) {
		super(entityManager);
	}

	private Vector3f temp1 = new Vector3f();
	private Vector3f temp2 = new Vector3f();

	public void update() {

		List<Entity> entities = entityManager.getEntitiesContainingComponent(AIBotComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			AIBotComponent aiBot = (AIBotComponent) entityManager.getComponentInEntity(entity, AIBotComponent.class);
			UnitComponent unit = (UnitComponent) entityManager.getComponentInEntity(entity, UnitComponent.class);
			MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
			aiBot.elapsed += GameServer.TIMESTEP;
			aiBot.timeLimit = 5000;
			if (aiBot.elapsed >= aiBot.timeLimit) {
				aiBot.elapsed -= aiBot.timeLimit;
				do {
					aiBot.targetLocation.set((float) Math.random() * 180 - 90, (float) Math.random() * 180 - 90, (float) Math.random() * 180 - 300);
				} while (unit.getPosition().distance(aiBot.targetLocation) < 40);
			}
			Vector3f direction = aiBot.targetLocation.sub(unit.getPosition(), temp1);
			// unit.lookAt(direction.normalize());

			movement.angularAcc.set(0, 0, 0);
			movement.angularAcc.add(0, 0.01f, 0.0025f);
			movement.angularVel.set(movement.angularAcc);
			unit.pitch(movement.angularVel.x);
			unit.yaw(movement.angularVel.y);
			unit.roll(movement.angularVel.z);

			movement.linearAcc.set(0);
			float speed = 0.0002f;
			movement.linearAcc.fma(speed, unit.getForward());
			movement.linearVel.add(movement.linearAcc);
			unit.getPosition().add(movement.linearVel);
		}
	}

}
