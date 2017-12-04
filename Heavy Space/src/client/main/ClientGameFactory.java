package client.main;

import org.joml.Vector3f;

import client.components.ActorComponent;
import client.components.SnapshotComponent;
import client.gameData.GameModelLoader;
import gameServer.components.ShipComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.AIBotComponent;
import shared.components.HealthComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.functionality.Event;

public class ClientGameFactory {

	private EntityManager entityManager;
	private GameModelLoader gameModelLoader;

	public ClientGameFactory(EntityManager entityManager, GameModelLoader gameModelLoader) {
		this.entityManager = entityManager;
		this.gameModelLoader = gameModelLoader;
	}

	public void setSkybox(Scene scene) {
		scene.setSkybox(gameModelLoader.skybox);
	}

	public Entity createBot(Vector3f position, float speed, float direction) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(5, 5, 5);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new AIBotComponent(speed, direction), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.fern), entity);
		return entity;
	}

	public Entity createShip(int entityVariation, Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(5, 5, 5);
		entityManager.addComponent(new HealthComponent(), entity);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ShipComponent(), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.dragon), entity);
		return entity;
	}

	public Entity createPlasmaProjectile(Vector3f position, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(3, 3, 3);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ProjectileComponent(null, 2000), entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.fern), entity);
		return entity;
	}

	public void createEntityFromEvent(Event event, GameModel gameModel) {
		int eeid = (int) event.data[1];
		Entity entity = gameModel.getEntity(eeid);
		if (entity != null)
			return;

		short tick = (short) event.data[0];
		int entityType = (int) event.data[2];
		int entityVariation = (int) event.data[3];
		Vector3f position = (Vector3f) event.data[4];

		switch (entityType) {
		case 0: {
			entity = createShip(entityVariation, position);
			entityManager.addComponent(new SnapshotComponent(eeid, tick, position), entity);
//			entityManager.addComponent(new SpawnComponent(tick), entity);
		}
			break;
		case 1: {
			Vector3f velocity = (Vector3f) event.data[5];
			switch (entityVariation) {
			case 0:
				entity = createPlasmaProjectile(position, velocity);
				break;
			default:
				break;
			}
			entityManager.addComponent(new SpawnComponent(tick), entity);
		}
			break;
		default:
			break;
		}

		gameModel.addEntity(eeid, entity);
	}

}
