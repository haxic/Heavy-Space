package gameServer.core;

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
	private short playerCounter;
	private EntityManager entityManager;

	public PlayerManager(EntityManager entityManager) {
		this.entityManager = entityManager;
		players = new HashMap<>();
	}

	public Entity getPlayer(String username) {
		return players.get(username);
	}

	public Entity createPlayer(String username) {
		Entity player = entityManager.createEntity();
		players.put(username, player);
		entityManager.addComponent(new PlayerComponent(++playerCounter), player);
		player.attach(this);
		return player;
	}

	@Override
	public void detach(Entity entity) {
	}

	public int getSize() {
		return players.size();
	}
}
