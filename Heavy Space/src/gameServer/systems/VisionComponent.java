package gameServer.systems;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hecs.Entity;
import hecs.EntityComponent;
import hecs.EntityContainer;

public class VisionComponent extends EntityComponent implements EntityContainer {
	private Set<Entity> createEntities;
	private Set<Entity> updateEntities;

	public VisionComponent() {
		createEntities = new HashSet();
		updateEntities = new HashSet();
	}

	public void createEntity(Entity entity) {
		createEntities.add(entity);
		entity.attach(this);
	}

	public void createEntities(List<Entity> entities) {
		for (Entity entity : entities)
			createEntity(entity);
	}

	public void updateEntity(Entity entity) {
		this.updateEntities.add(entity);
		entity.attach(this);
	}

	public void updateEntities(List<Entity> entities) {
		for (Entity entity : createEntities)
			updateEntity(entity);
	}

	public void clearCreateEntities() {
		for (Entity entity : createEntities)
			entity.detach(this);
		createEntities.clear();
	}
	
	public void clearUpdateEntities() {
		for (Entity entity : updateEntities)
			entity.detach(this);
		updateEntities.clear();
	}

	@Override
	protected void removeComponent() {
		clearCreateEntities();
		clearUpdateEntities();
	}

	public Set<Entity> getCreateEntities() {
		return createEntities;
	}

	public Set<Entity> getUpdateEntities() {
		return updateEntities;
	}

	@Override
	public void detach(Entity entity) {
		updateEntities.remove(entity);
		createEntities.remove(entity);
	}

}
