package entities;

import models.Model;

public class Actor {

	private Entity entity;
	private Model model;

	public Actor(Entity entity, Model model) {
		this.entity = entity;
		this.model = model;
	}
	
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
}
