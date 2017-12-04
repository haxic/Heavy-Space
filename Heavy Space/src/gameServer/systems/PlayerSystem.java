package gameServer.systems;

import java.util.List;

import org.joml.Vector3f;

import client.main.ServerGameFactory;
import gameServer.components.PlayerComponent;
import gameServer.components.ShipComponent;
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

				System.out.println(entity.getEID() + " spawn ship (have already:)" + shipEntity);
				shipEntity = serverGameFactory.createShip(new Vector3f(0, 0, 0));
				entityManager.addComponent(new HealthComponent(), entity);
				entityManager.addComponent(new SpawnComponent((short) 0), shipEntity);
				playerComponent.controlShip(shipEntity);
			} else if (shipEntity != null) {
				HealthComponent healthComponent = (HealthComponent) entityManager.getComponentInEntity(shipEntity, HealthComponent.class);
				// TODO: tell client that current ship is still functional
				if (healthComponent.coreIntegrity > 0)
					return;
				ObjectComponent unitComponent = (ObjectComponent) entityManager.getComponentInEntity(shipEntity, ObjectComponent.class);
				MovementComponent movementComponent = (MovementComponent) entityManager.getComponentInEntity(shipEntity, MovementComponent.class);
				unitComponent.getPosition().set(new Vector3f(0, 0, 0));
				movementComponent.getLinearVel().set(new Vector3f(0, 0, 0));
				healthComponent.coreIntegrity = healthComponent.coreIntegrityMax;
			}
			playerComponent.resetRequests();
		}
	}
}
