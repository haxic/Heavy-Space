package client.gameData;

import org.joml.Vector3f;

import client.components.ActorComponent;
import client.components.SnapshotComponent;
import client.entities.LightComponent;
import client.entities.Scene;
import gameServer.components.ShipComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.AIBotComponent;
import shared.components.CollisionComponent;
import shared.components.HealthComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.functionality.Event;
import shared.functionality.EventType;

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
	
	public Entity createDebris(Vector3f position, Vector3f scale) {
		Entity entity = entityManager.createEntity();
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.sphere), entity);
		return entity;
	}

	public Entity createBox(Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.sphere), entity);
		return entity;
	}

	public Entity createObstacle(Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(200, 200, 200);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.sphere), entity);
		return entity;
	}

	public Entity createBot(Vector3f position, float speed, float direction) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(30, 30, 30);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new AIBotComponent(speed, direction), entity);
		entityManager.addComponent(new ShipComponent(), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.sphere), entity);
		return entity;
	}

	public Entity createShip(int entityVariation, Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(30, 30, 30);
		entityManager.addComponent(new HealthComponent(), entity);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ShipComponent(), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.sphere), entity);
		return entity;
	}

	public Entity createCannonProjectile(short ownerEntityID, Vector3f position, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(2, 2, 2);

		// TODO: ownerEntityID fix gameModel etc
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ProjectileComponent(null, (byte) 0, 4f, 50), entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.sphere), entity);
		return entity;
	}

	public Entity createPlasmaProjectile(short ownerEntityID, Vector3f position, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(5, 5, 5);

		// TODO: ownerEntityID fix gameModel etc
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ProjectileComponent(null, (byte) 1, 4f, 400), entity);
		// entityManager.addComponent(new CollisionComponent(5f), entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		entityManager.addComponent(new ParticleComponent(gameModelLoader.particleAtlasTexture, 0, 0.01f, 1, 20), entity);
		entityManager.addComponent(new LightComponent(new Vector3f(0f, 0.5f, 0.5f), new Vector3f(1000f, 0f, 0f)), entity);
		return entity;
	}

	public Entity createSun(Vector3f position, Vector3f color) {
		Entity entity = entityManager.createEntity();
		entityManager.addComponent(new ObjectComponent(position, new Vector3f(2000, 2000, 2000)), entity);
		entityManager.addComponent(new LightComponent(color, new Vector3f(10000000, 0, 0)), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.cube), entity);
		return entity;
	}

	public short createEntityFromEvent(Event event, GameModel gameModel) {
//		eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_CREATE_UNIT, tick, eeid, entityType, entityVariation, playerID, position, forward, up, velocity));
		int eeid = (int) event.data[1];
		short tick = (short) event.data[0];
		Entity entity = gameModel.getEntity(eeid);
		if (entity != null)
			return tick;

		int entityType = (int) event.data[2];
		int entityVariation = (int) event.data[3];
		Vector3f position = (Vector3f) event.data[5];
		Vector3f forward = (Vector3f) event.data[6];
		Vector3f up = (Vector3f) event.data[7];
		
		switch (entityType) {
		case 0: {
			short ownerEntityID = (short) event.data[4];
			entity = createShip(entityVariation, position);
			entityManager.addComponent(new SnapshotComponent(ownerEntityID, tick, position, forward, up), entity);
			entityManager.addComponent(new SpawnComponent(tick), entity);
		}
			break;
		case 1: {
			short ownerEntityID = (short) event.data[4];
			Vector3f velocity = (Vector3f) event.data[8];
			switch (entityVariation) {
			case 0:
				entity = createCannonProjectile(ownerEntityID, position, velocity);
				break;
			case 1:
				entity = createPlasmaProjectile(ownerEntityID, position, velocity);
				break;
			default:
				break;
			}
			entityManager.addComponent(new SpawnComponent(tick), entity);
		}
			break;
		case 2: {
			entity = createObstacle(position);
			entityManager.addComponent(new SpawnComponent(), entity);
		}
			break;
		default:
			break;
		}

		gameModel.addEntity(eeid, entity);
		return tick;
	}

	public void createLotsOfDebris(Scene scene, int number) {
		for (int i = 0; i < number; i++) {
			Vector3f position = new Vector3f((float) (Math.random() * 5000 - 2500), (float) (Math.random() * 5000 - 2500), (float) (Math.random() * 5000 - 2500));
			Vector3f scale = new Vector3f((float) (Math.random() * 1.5f + 2.5f));
			Entity debris = createDebris(position, scale);
			scene.addActorEntity(debris);
		}
	}


}
