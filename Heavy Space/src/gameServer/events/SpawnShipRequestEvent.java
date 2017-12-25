package gameServer.events;

import hecs.Entity;
import hevent.Event;

public class SpawnShipRequestEvent extends Event {

	public Entity entity;

	public SpawnShipRequestEvent(Entity entity) {
		this.entity = entity;
	}

}
