package gameServer.systems;

import java.util.List;

import org.joml.Vector3f;

import gameServer.components.PlayerComponent;
import gameServer.components.ShipComponent;
import gameServer.core.ServerGameFactory;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.HealthComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.SpawnComponent;

public class PlayerSystem {

	private EntityManager entityManager;
	private ServerGameFactory serverGameFactory;

	public PlayerSystem(EntityManager entityManager, ServerGameFactory serverGameFactory) {
		this.entityManager = entityManager;
		this.serverGameFactory = serverGameFactory;
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(PlayerComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(entity, PlayerComponent.class);
			Entity shipEntity = playerComponent.getShip();
			if (playerComponent.isRequestingSpawnShip() && shipEntity == null) {
				Vector3f position;
				int side = (int) (Math.random() * 6) + 1;
				int offsetA = (int) (Math.random() * 1500) - 750;
				int offsetB = (int) (Math.random() * 1500) -750;
				switch (side) {
				case 1:
					position = new Vector3f(-3000, offsetA, offsetB);
					break;
				case 2:
					position = new Vector3f(3000, offsetA, offsetB);
					break;
				case 3:
					position = new Vector3f(offsetA, -3000, offsetB);
					break;
				case 4:
					position = new Vector3f(offsetA, 3000, offsetB);
					break;
				case 5:
					position = new Vector3f(offsetA, offsetB, -3000);
					break;
				case 6:
					position = new Vector3f(offsetA, offsetB, 3000);
					break;
				default:
					position = new Vector3f(0, 0, 0);
					break;
				}
				shipEntity = serverGameFactory.createShip(position, entity);
				entityManager.addComponent(new HealthComponent(), entity);
				playerComponent.controlShip(shipEntity);
			}

			// else if (shipEntity != null) {
			// HealthComponent healthComponent = (HealthComponent) entityManager.getComponentInEntity(shipEntity, HealthComponent.class);
			// // TODO: tell client that current ship is still functional
			// if (healthComponent.getCoreIntegrity() > 0)
			// return;
			// ObjectComponent unitComponent = (ObjectComponent) entityManager.getComponentInEntity(shipEntity, ObjectComponent.class);
			// MovementComponent movementComponent = (MovementComponent) entityManager.getComponentInEntity(shipEntity, MovementComponent.class);
			// unitComponent.getPosition().set(new Vector3f(0, 0, 0));
			// movementComponent.getLinearVel().set(new Vector3f(0, 0, 0));
			// healthComponent.setCoreIntegrity(healthComponent.getCoreIntegrityMax());
			// }
			playerComponent.resetRequests();
		}
	}
}
