package gameServer.systems;

import java.util.List;

import org.joml.Vector3f;

import gameServer.components.PlayerComponent;
import gameServer.components.ShipComponent;
import gameServer.core.ServerGameFactory;
import gameServer.events.EntityCreatedEvent;
import gameServer.events.PlayerActionEvent;
import gameServer.events.SpawnShipRequestEvent;
import hecs.Entity;
import hecs.EntityManager;
import hevent.Event;
import hevent.EventListener;
import hevent.EventManager;
import shared.components.HealthComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.SpawnComponent;
import tests.functional.AreaEnteredEvent;
import tests.functional.DamageReceivedEvent;

public class PlayerSystem implements EventListener {

	private EntityManager entityManager;
	private EventManager eventManager;
	private ServerGameFactory serverGameFactory;
	private Vector3f tempVector = new Vector3f();

	public PlayerSystem(EntityManager entityManager, EventManager eventManager, ServerGameFactory serverGameFactory) {
		this.entityManager = entityManager;
		this.eventManager = eventManager;
		eventManager.subscribe(PlayerActionEvent.class, this);
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
				int offsetB = (int) (Math.random() * 1500) - 750;
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
				playerComponent.controlShip(shipEntity);
				eventManager.createEvent(new EntityCreatedEvent(shipEntity));
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

	@Override
	public void handleEvent(Event event) {
		if (event instanceof PlayerActionEvent) {
			PlayerActionEvent playerActionEvent = (PlayerActionEvent) event;
			controlShip(playerActionEvent.player, playerActionEvent.actions, playerActionEvent.angularVelocity, playerActionEvent.angularVelocityDT, playerActionEvent.dt);
		} else if (event instanceof SpawnShipRequestEvent) {
			SpawnShipRequestEvent playerSpawnEvent = (SpawnShipRequestEvent) event;
			createShip(playerSpawnEvent.entity);
		}
	}
	
	private void createShip(Entity player) {
		PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(player, PlayerComponent.class);
		playerComponent.requestSpawnShip();
	}
	
	private void controlShip(Entity player, boolean[] actions, Vector3f angularVelocity, float angularVelocityDT, float dt) {
		PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(player, PlayerComponent.class);
		Entity shipEntity = playerComponent.getShip();
		if (shipEntity == null)
			return;
		ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(shipEntity, ShipComponent.class);
		// Fire primary
		if (actions[6])
			shipComponent.requestFirePrimary();
		if (actions[7])
			shipComponent.requestFireSecondary();
		try {
			ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(shipEntity, ObjectComponent.class);
			// System.out.println(
			// "RECEIVE VELOCITY: " + shipComponent.getLinearThrust() + " " + objectComponent.getPosition() + " " + movementComponent.getLinearVel() +
			// " " + movementComponent.getLinearAcc());

			Vector3f linearDirection = new Vector3f();
			if (actions[0])
				linearDirection.z++;
			if (actions[1])
				linearDirection.z--;
			if (actions[2])
				linearDirection.x++;
			if (actions[3])
				linearDirection.x--;
			if (actions[4])
				linearDirection.y++;
			if (actions[5])
				linearDirection.y--;

			shipComponent.getLinearThrust().add(objectComponent.getForward().mul(dt * 2 * linearDirection.z, tempVector ));
			shipComponent.getLinearThrust().add(objectComponent.getRight().mul(dt * 2 * linearDirection.x, tempVector));
			shipComponent.getLinearThrust().add(objectComponent.getUp().mul(dt * 2 * linearDirection.y, tempVector));

			// objectComponent.yaw(dt * angularVelocity.y);
			// objectComponent.pitch(dt * angularVelocity.x);
			// objectComponent.roll(dt * angularVelocity.z);
			objectComponent.rotate(angularVelocityDT * angularVelocity.x, angularVelocityDT * angularVelocity.y, angularVelocityDT * angularVelocity.z);

			// System.out.println("UDP RE: " + shipComponent.getLinearThrust());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
