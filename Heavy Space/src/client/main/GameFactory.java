package client.main;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import client.components.ActorComponent;
import client.gameData.GameModelLoader;
import gameServer.components.AIBotComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.MovementComponent;
import shared.components.UnitComponent;

public class GameFactory {
	EntityManager entityManager;
	GameModelLoader gameModelLoader;
	private boolean server;

	public GameFactory(EntityManager entityManager, GameModelLoader gameModelLoader, boolean server) {
		this.entityManager = entityManager;
		this.gameModelLoader = gameModelLoader;
		this.server = server;
	}

	public void setSkybox(Scene scene) {
		scene.setSkybox(gameModelLoader.skybox);
	}

	public Entity createDragon() {
		Entity entity = entityManager.createEntity();
		Vector3f position = new Vector3f(0, -5, -20);
		Vector3f rotation = new Vector3f();
		Vector3f scale = new Vector3f(1, 1, 1);
		entityManager.addComponent(new UnitComponent(position, rotation, scale), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.dragon), entity);
		return entity;
	}

	public Entity createBot(Vector3f position, Vector3f rotation) {
		Entity entity = entityManager.createEntity();
		Vector3f scale = new Vector3f(5, 5, 5);
		entityManager.addComponent(new UnitComponent(position, rotation, scale), entity);
		entityManager.addComponent(new AIBotComponent(10000, 0.00001f), entity);
		entityManager.addComponent(new MovementComponent(), entity);
		if (!server) {
			entityManager.addComponent(new ActorComponent(gameModelLoader.dragon), entity);
		}
		return entity;
	}

}
