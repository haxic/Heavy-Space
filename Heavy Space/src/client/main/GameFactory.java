package client.main;

import org.joml.Vector3f;

import client.components.ActorComponent;
import client.gameData.GameModelLoader;
import gameServer.components.AIBotComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.HealthComponent;
import shared.components.MovementComponent;
import shared.components.UnitComponent;

public class GameFactory {
	EntityManager entityManager;
	GameModelLoader gameModelLoader;

	public GameFactory(EntityManager entityManager, GameModelLoader gameModelLoader) {
		this.entityManager = entityManager;
		this.gameModelLoader = gameModelLoader;
	}

	public void setSkybox(Scene scene) {
		scene.setSkybox(gameModelLoader.skybox);
	}

	public Entity createDragon() {
		Entity entity = entityManager.createEntity();
		Vector3f position = new Vector3f(0, -5, -20);
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new UnitComponent(position, scale), entity);
		if (gameModelLoader != null) {
			entityManager.addComponent(new ActorComponent(gameModelLoader.dragon), entity);
		}
		return entity;
	}

	public Entity createBot(Vector3f position, Vector3f direction, float speed) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(5, 5, 5);
		entityManager.addComponent(new UnitComponent(position, scale), entity);
		entityManager.addComponent(new AIBotComponent(10000, 0.00001f), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		if (gameModelLoader != null) {
			entityManager.addComponent(new ActorComponent(gameModelLoader.dragon), entity);
		}
		return entity;
	}

	public Entity createShip(Vector3f position) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new HealthComponent(), entity);
		entityManager.addComponent(new UnitComponent(position, scale), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		if (gameModelLoader != null) {
			entityManager.addComponent(new ActorComponent(gameModelLoader.dragon), entity);
		}
		return entity;
	}

}
