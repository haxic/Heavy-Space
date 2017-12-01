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
			UnitComponent unit = (UnitComponent) entityManager.getComponentInEntity(entity, UnitComponent.class);
			MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
			aiBot.elapsed += Globals.dt;
			if (aiBot.elapsed >= aiBot.timeLimit) {
				aiBot.elapsed -= aiBot.timeLimit;
				do {
					aiBot.targetLocation.set((float) Math.random() * 180 - 90, (float) Math.random() * 180 - 90, (float) Math.random() * 180 - 300);
				} while (unit.getPosition().distance(aiBot.targetLocation) < 40);
			}

			Vector3f angularThrust = new Vector3f(0, 0, 0.025f);

			movement.getAngularAcc().zero();
			movement.getAngularAcc().add(angularThrust);
			movement.getAngularVel().fma(Globals.dt, movement.getAngularAcc());
			unit.pitch(Globals.dt * movement.getAngularVel().x);
			unit.yaw(Globals.dt * movement.getAngularVel().y);
			unit.roll(Globals.dt * movement.getAngularVel().z);

			int distance = 20;
			Vector3f linearThrust = unit.getForward(tempVector).mul(100);

			movement.getLinearAcc().zero();
			movement.getLinearAcc().add(linearThrust);
			movement.getLinearVel().fma(Globals.dt, movement.getLinearAcc());
			unit.getPosition().fma(Globals.dt, movement.getLinearVel());

			unit.getPosition().fma(Globals.dt, linearThrust);

			if (unit.getPosition().z > distance || unit.getPosition().z < -distance) {
				if (unit.getPosition().z > distance) {
					unit.getPosition().z = distance;
					unit.getForward().set(0, 0, -1);
				} else {
					unit.getPosition().z = -distance;
					unit.getForward().set(0, 0, 1);
				}
			}

		}
	}

}
