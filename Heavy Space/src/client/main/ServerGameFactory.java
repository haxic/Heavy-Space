package client.main;

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

	public Entity createBot(Vector3f position, float speed, float direction) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(5, 5, 5);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new AIBotComponent(speed, direction), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		entityManager.addComponent(new CollisionComponent(5), entity);
		return entity;
	}

	public Entity createShip(Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(5, 5, 5);
		entityManager.addComponent(new HealthComponent(), entity);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		entityManager.addComponent(new ShipComponent(), entity);
		return entity;
	}

	public Entity createPlasmaProjectile(Entity shipEntity, Vector3f position, Vector3f velocity) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new ObjectComponent(position, scale), entity);
		ProjectileComponent projectile = new ProjectileComponent(shipEntity, 2000);
		projectile.activate();
		entityManager.addComponent(new CollisionComponent(1), entity);
		entityManager.addComponent(projectile, entity);
		MovementComponent movementComponent = new MovementComponent();
		movementComponent.getLinearVel().set(velocity);
		entityManager.addComponent(movementComponent, entity);
		return entity;
	}
}
