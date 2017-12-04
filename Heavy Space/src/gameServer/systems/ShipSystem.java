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

public class ShipSystem {

	private EntityManager entityManager;
	private ServerGameFactory serverGameFactory;

	public ShipSystem(EntityManager entityManager, ServerGameFactory serverGameFactory) {
		this.entityManager = entityManager;
		this.serverGameFactory = serverGameFactory;
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(ShipComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(entity, ShipComponent.class);
			if (shipComponent.isRequestFirePrimary()) {
				Entity plasmaProjectile = serverGameFactory.createPlasmaProjectile(entity, new Vector3f(shipComponent.getPosition()), new Vector3f(shipComponent.getDirection()).mul(1000));
				entityManager.addComponent(new SpawnComponent((short) 0), plasmaProjectile);
			}

			// HealthComponent healthComponent = (HealthComponent) entityManager.getComponentInEntity(entity, HealthComponent.class);
			// if (healthComponent.coreIntegrity <= 0)
			// return;
			ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			// MovementComponent movementComponent = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
			objectComponent.getPosition().set(shipComponent.getPosition());
			shipComponent.resetRequests();
		}
	}
}
