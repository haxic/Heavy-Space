package gameServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import gameServer.components.PlayerComponent;

import java.util.Set;

import hecs.Entity;
import hecs.EntityContainer;
import hecs.EntityManager;

public class PlayerManager implements EntityContainer {
	public Map<String, Entity> players;
	private int playerCounter;
	private EntityManager entityManager;

	public PlayerManager(EntityManager entityManager) {
		this.entityManager = entityManager;
		players = new HashMap<>();
	}

	public Entity getPlayerByUUID(String uuid) {
		return players.get(uuid);
	}

	public Entity createPlayer(String uuid) {
		Entity player = entityManager.createEntity();
		players.put(uuid, player);
		entityManager.addComponent(new PlayerComponent(playerCounter++), player);
		player.attach(this);
		return player;
	}

	@Override
	public void detach(Entity entity) {
	}
}
