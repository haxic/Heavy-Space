package gameServer.events;

import hecs.Entity;
import hevent.Event;

public class PlayerSpawnEvent extends Event {

	public Entity entity;

	public PlayerSpawnEvent(Entity entity) {
		this.entity = entity;
	}

}
