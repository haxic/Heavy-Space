package gameServer;

import org.joml.Vector3f;

import client.gameData.GameModelLoader;
import gameServer.components.ShipComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.AIBotComponent;
import shared.components.CollisionComponent;
import shared.components.HealthComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;

public class ServerGameFactory {
	EntityManager entityManager;

	public ServerGameFactory(EntityManager entityManager, GameModelLoader gameModelLoader) {
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
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new HealthComponent(), entity);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		entityManager.addComponent(new CollisionComponent(new Vector3f(position), 30f), entity);
		ShipComponent shipComponent;
		if (player == null)
			shipComponent = new ShipComponent();
		else
			shipComponent = new ShipComponent(player);
		entityManager.addComponent(shipComponent, entity);
		return entity;
	}

	public Entity createCannonProjectile(Entity shipEntity, Vector3f position, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		ProjectileComponent projectile = new ProjectileComponent(shipEntity, (byte) 0, 1f, 400);
		entityManager.addComponent(new CollisionComponent(new Vector3f(position), 4f), entity);
		entityManager.addComponent(projectile, entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		return entity;
	}

	public Entity createPlasmaProjectile(Entity shipEntity, Vector3f position, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		ProjectileComponent projectile = new ProjectileComponent(shipEntity, (byte) 1, 1f, 400);
		entityManager.addComponent(new CollisionComponent(new Vector3f(position), 8f), entity);
		entityManager.addComponent(projectile, entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		return entity;
	}


}
