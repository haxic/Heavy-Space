package gameServer.components;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hecs.Entity;
import hecs.EntityComponent;

public class ClientGameDataTransferComponent extends EntityComponent {

	private Set<Entity> createUnits;
	private Set<Entity> updateUnits;

	public ClientGameDataTransferComponent() {
		createUnits = new HashSet();
		updateUnits = new HashSet();
	}

	public void createUnits(List<Entity> units) {
		this.createUnits.addAll(units);
	}

	public void updateUnits(List<Entity> units) {
		this.updateUnits.addAll(units);
		for (Entity entity : createUnits) {
			updateUnits.remove(entity);
		}
	}

	public void clear() {
		createUnits.clear();
		updateUnits.clear();
	}

	@Override
	protected void removeComponent() {
		clear();
	}

	public Set<Entity> getCreateEntities() {
		return createUnits;
	}

	public Set<Entity> getUpdateEntities() {
		return updateUnits;
	}
}
