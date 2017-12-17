package client.gameData;

import org.joml.Vector3f;

import client.components.ActorComponent;
import client.components.LightComponent;
import client.components.ParticleComponent;
import client.components.SnapshotComponent;
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
	private GameAssetLoader gameAssetLoader;

	public ClientGameFactory(EntityManager entityManager, GameAssetLoader gameAssetLoader) {
		this.entityManager = entityManager;
		this.gameAssetLoader = gameAssetLoader;
	}

	public void setSkybox(Scene scene) {
		scene.setSkybox(gameAssetLoader.skybox);
	}

	public Entity createDebris(Vector3f position, Vector3f scale) {
		Entity entity = entityManager.createEntity();
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.iceAsteroidSmall), entity);
		return entity;
	}

	public Entity createBox(Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.sphere), entity);
		return entity;
	}

	public Entity createObstacle(Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(200, 200, 200);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.iceAsteroid), entity);
		return entity;
	}

	public Entity createBot(Vector3f position, float speed, float direction) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(30, 30, 30);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new AIBotComponent(speed, direction), entity);
		entityManager.addComponent(new ShipComponent(), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.sphere), entity);
		return entity;
	}

	public Entity createShip(int entityVariation, Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(30, 30, 30);
		entityManager.addComponent(new HealthComponent(), entity);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ShipComponent(), entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.sphere), entity);
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
		entityManager.addComponent(new ActorComponent(gameAssetLoader.sphere), entity);
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
		entityManager.addComponent(new ParticleComponent(gameAssetLoader.particleAtlasTexture, 0, 0.01f, 1, 20), entity);
		entityManager.addComponent(new LightComponent(new Vector3f(0f, 0.5f, 0.5f), new Vector3f(1000f, 0f, 0f)), entity);
		return entity;
	}

	public Entity createSun(Vector3f position, Vector3f color) {
		Entity entity = entityManager.createEntity();
		entityManager.addComponent(new ObjectComponent(position, new Vector3f(2000, 2000, 2000)), entity);
		entityManager.addComponent(new LightComponent(color, new Vector3f(10000000, 0, 0)), entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.cube), entity);
		return entity;
	}

	public short createEntityFromEvent(Event event, GameModel gameModel) {
		// eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_CREATE_UNIT, tick, eeid, entityType, entityVariation, playerID, position, forward,
		// up, velocity));
		short tick = (short) event.data[0];
		int eeid = (int) event.data[1];
		Entity entity = gameModel.getEntity(eeid);
		if (entity != null)
			return tick;
		int entityType = (int) event.data[2];
		int entityVariation = (int) event.data[3];
		short ownerEntityID = (short) event.data[4];
		Vector3f position = (Vector3f) event.data[5];
		Vector3f forward = (Vector3f) event.data[6];
		Vector3f up = (Vector3f) event.data[7];
		Vector3f right = (Vector3f) event.data[8];
		Vector3f velocity = (Vector3f) event.data[9];
		entity = prepareSpawnEntity(tick, entityType, entityVariation, ownerEntityID, position, forward, up, right, velocity);
		entityManager.addComponent(new ObjectComponent(position), entity);
		gameModel.addEntity(eeid, entity);
		return tick;
	}

	private Entity prepareSpawnEntity(short tick, int entityType, int entityVariation, short ownerEntityID, Vector3f position, Vector3f forward, Vector3f up, Vector3f right, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		entityManager.addComponent(new SpawnComponent(tick, entityType, entityVariation, ownerEntityID, position, forward, up, right, velocity), entity);
		switch (entityType) {
		case 0: {
			entityManager.addComponent(new SnapshotComponent(ownerEntityID, tick, position, forward, up, right), entity);
		}
			break;
		default:
			break;
		}
		return entity;
	}

	public void createLotsOfDebris(Scene scene, int number) {
		for (int i = 0; i < number; i++) {
			Vector3f position = new Vector3f((float) (Math.random() * 5000 - 2500), (float) (Math.random() * 5000 - 2500), (float) (Math.random() * 5000 - 2500));
			Vector3f scale = new Vector3f((float) (Math.random() * 1.5f + 2.5f));
			Entity debris = createDebris(position, scale);
			scene.addActorEntity(debris);
		}
	}

	public void spawnEntity(Entity entity, SpawnComponent spawnComponent) {
		switch (spawnComponent.getEntityType()) {
		case 0: {
			SnapshotComponent snapshot = (SnapshotComponent) entityManager.getComponentInEntity(entity, SnapshotComponent.class);
			spawnShip(entity, spawnComponent.getEntityVariation(), spawnComponent.getPosition(), spawnComponent.getForward(), spawnComponent.getUp(), spawnComponent.getRight());
		}
			break;
		case 1: {
			spawnProjectile(entity, spawnComponent.getEntityVariation(), spawnComponent.getPosition(), spawnComponent.getForward(), spawnComponent.getUp(), spawnComponent.getRight(),
					spawnComponent.getVelocity());
			switch (spawnComponent.getEntityVariation()) {
			case 0:
				spawnCannonProjectile(entity, spawnComponent.getPosition(), spawnComponent.getVelocity());
				break;
			case 1:
				spawnPlasmaProjectile(entity, spawnComponent.getPosition(), spawnComponent.getVelocity());
				break;
			default:
				break;
			}
		}
			break;
		case 2: {
			spawnObstacle(entity, spawnComponent.getPosition());
		}
			break;
		default:
			break;
		}

		entityManager.removeComponentAll(SpawnComponent.class, entity);
	}

	private void spawnShip(Entity entity, int entityVariation, Vector3f position, Vector3f forward, Vector3f up, Vector3f right) {
		Vector3f scale = new Vector3f(30, 30, 30);
		entityManager.addComponent(new HealthComponent(), entity);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ShipComponent(), entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.ship), entity);
	}

	private void spawnProjectile(Entity entity, int entityVariation, Vector3f position, Vector3f forward, Vector3f up, Vector3f right, Vector3f velocity) {
		switch (entityVariation) {
		case 0:
			spawnCannonProjectile(entity, position, velocity);
			break;
		case 1:
			spawnPlasmaProjectile(entity, position, velocity);
			break;
		default:
			break;
		}
	}

	public Entity spawnCannonProjectile(Entity entity, Vector3f position, Vector3f velocity) {
		Vector3f scale = new Vector3f(0.4f, 0.4f, 0.4f);

		// TODO: ownerEntityID fix gameModel etc
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ProjectileComponent(null, (byte) 0, 4f, 50), entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.cannonProjectile), entity);
		return entity;
	}

	public Entity spawnPlasmaProjectile(Entity entity, Vector3f position, Vector3f velocity) {
		Vector3f scale = new Vector3f(5, 5, 5);

		// TODO: ownerEntityID fix gameModel etc
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ProjectileComponent(null, (byte) 1, 4f, 400), entity);
		// entityManager.addComponent(new CollisionComponent(5f), entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		entityManager.addComponent(new ParticleComponent(gameAssetLoader.particleAtlasTexture, 0, 0.01f, 1, 20), entity);
		entityManager.addComponent(new LightComponent(new Vector3f(0f, 0.5f, 0.5f), new Vector3f(1000f, 0f, 0f)), entity);
		return entity;
	}
	
	public Entity spawnObstacle(Entity entity, Vector3f position) {
		Vector3f scale = new Vector3f(200, 200, 200);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new ActorComponent(gameAssetLoader.iceAsteroid), entity);
		return entity;
	}

}
