package gameServer.core;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import client.gameData.GameAssetLoader;
import gameServer.components.ObstacleComponent;
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

public class ServerGameFactory {
	EntityManager entityManager;

	public ServerGameFactory(EntityManager entityManager, GameAssetLoader gameAssetLoader) {
		this.entityManager = entityManager;
	}

	public Entity createObstacle(Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new CollisionComponent(new Vector3f(position), 200f), entity);
		entityManager.addComponent(new ObstacleComponent(), entity);
		System.out.println("CREATE OBSTACLE: " + entity.getEID());
		return entity;
	}

	public Entity createBot(Vector3f position, float speed, float direction) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new AIBotComponent(speed, direction), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		entityManager.addComponent(new CollisionComponent(new Vector3f(position), 30f), entity);
		entityManager.addComponent(new ShipComponent(), entity);
		System.out.println("CREATE BOT: " + entity.getEID());
		return entity;
	}

	public Entity createShip(Vector3f position, Entity player) {
		Entity entity = entityManager.createEntity();
		System.out.println("CREATE SHIP: " + entity.getEID());
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new HealthComponent(), entity);
		ObjectComponent objectComponent = new ObjectComponent(position, scale);
		entityManager.addComponent(objectComponent, entity);
		entityManager.addComponent(new MovementComponent(), entity);
		entityManager.addComponent(new CollisionComponent(new Vector3f(position), 30f), entity);
		ShipComponent shipComponent;
		if (player == null)
			shipComponent = new ShipComponent();
		else
			shipComponent = new ShipComponent(player);
		entityManager.addComponent(shipComponent, entity);
//		entityManager.addComponent(new SpawnComponent((short) 0, 0, 0, (short) 0, new Vector3f(position), new Vector3f(objectComponent.getForward()), new Vector3f(objectComponent.getUp()), new Vector3f(objectComponent.getRight()), null), entity);
		entityManager.addComponent(new SpawnComponent((short) 0, 0, 0, (short) 0, new Vector3f(position), new Quaternionf(objectComponent.getOrientation()), null), entity);
		return entity;
	}

	public Entity createCannonProjectile(Entity shipEntity, Vector3f position, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		ProjectileComponent projectile = new ProjectileComponent(shipEntity, (byte) 0, 4f, 400);
		entityManager.addComponent(new CollisionComponent(new Vector3f(position), 4f), entity);
		entityManager.addComponent(projectile, entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		entityManager.addComponent(new SpawnComponent((short) 0, 0, 0, (short) 0, new Vector3f(position), null, new Vector3f(velocity)), entity);
		return entity;
	}

	public Entity createPlasmaProjectile(Entity shipEntity, Vector3f position, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		ProjectileComponent projectile = new ProjectileComponent(shipEntity, (byte) 1, 4f, 400);
		entityManager.addComponent(new CollisionComponent(new Vector3f(position), 8f), entity);
		entityManager.addComponent(projectile, entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		entityManager.addComponent(new SpawnComponent((short) 0, 0, 0, (short) 0, new Vector3f(position), null, new Vector3f(velocity)), entity);
		return entity;
	}

}
