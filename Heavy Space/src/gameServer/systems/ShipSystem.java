package gameServer.systems;

import java.util.List;

import org.joml.Vector3f;

import gameServer.components.PlayerComponent;
import gameServer.components.ShipComponent;
import gameServer.core.ServerGameFactory;
import gameServer.events.EntityCreatedEvent;
import hecs.Entity;
import hecs.EntityManager;
import hevent.EventManager;
import shared.components.HealthComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.SpawnComponent;

public class ShipSystem {

	private EntityManager entityManager;
	private ServerGameFactory serverGameFactory;
	private EventManager eventManager;

	public ShipSystem(EntityManager entityManager, EventManager eventManager, ServerGameFactory serverGameFactory) {
		this.entityManager = entityManager;
		this.eventManager = eventManager;
		this.serverGameFactory = serverGameFactory;
	}

	public void process(float dt) {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(ShipComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(entity, ShipComponent.class);
			ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			MovementComponent movementComponent = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
			shipComponent.update(dt);
			if (shipComponent.isRequestFirePrimary()) {
				shipComponent.firePrimary();
				Vector3f forward = new Vector3f((float) Math.random() * 0.04f - 0.02f, (float) Math.random() * 0.04f - 0.02f, (float) Math.random() * 0.04f - 0.02f).add(objectComponent.getForward())
						.normalize();
				Entity projectileEntity = serverGameFactory.createCannonProjectile(entity, new Vector3f(objectComponent.getPosition()), forward.mul(1200).add(movementComponent.getLinearVel()));
				eventManager.createEvent(new EntityCreatedEvent(projectileEntity));
			}
			if (shipComponent.isRequestFireSecondary()) {
				shipComponent.fireSecondary();
				Entity projectileEntity = serverGameFactory.createPlasmaProjectile(entity, new Vector3f(objectComponent.getPosition()),
						new Vector3f(objectComponent.getForward()).mul(750).add(movementComponent.getLinearVel()));
				eventManager.createEvent(new EntityCreatedEvent(projectileEntity));
			}

			// HealthComponent healthComponent = (HealthComponent) entityManager.getComponentInEntity(entity, HealthComponent.class);
			// if (healthComponent.coreIntegrity <= 0)
			// return;
			// MovementComponent movementComponent = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
			// System.out.println("SHIT SYSTEM: " + entity.getEID() + " " + shipComponent.getLinearThrust());
			movementComponent.getLinearAcc().add(shipComponent.getLinearThrust().mul(2500, new Vector3f()));
			shipComponent.resetRequests();
		}
	}
}
