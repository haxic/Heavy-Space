package client.main;

import org.joml.Vector3f;

import client.components.ActorComponent;
import client.gameData.GameModelLoader;
import hecs.Entity;
import hecs.EntityManager;
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
		entityManager.addComponent(new UnitComponent(new Vector3f(0, -5, -20), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), entity);
		entityManager.addComponent(new ActorComponent(gameModelLoader.dragon), entity);
		return entity;
	}

}
