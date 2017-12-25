package gameServer.events;

import hecs.Entity;
import hevent.Event;

public class EntityCreatedEvent extends Event {

	private Entity entity;

	public EntityCreatedEvent(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

}
