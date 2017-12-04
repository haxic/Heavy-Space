package shared.components;

import hecs.Entity;
import hecs.EntityComponent;
import hecs.EntityContainer;
import shared.functionality.Globals;

public class ProjectileComponent extends EntityComponent implements EntityContainer {
	long created;
	int length;
	private Entity shipEntity;

	public ProjectileComponent(Entity shipEntity, int length) {
		if (shipEntity != null) {
			this.shipEntity = shipEntity;
			shipEntity.attach(this);
		}
		this.length = length;
	}

	@Override
	protected void removeComponent() {
	}

	public void activate() {
		created = Globals.now;
	}

	public boolean hasElapsed() {
		return Globals.now - created > length;
	}

	public int remaining() {
		return (int) (length - (Globals.now - created));
	}

	public Entity getShipEntity() {
		return shipEntity;
	}

	@Override
	public void detach(Entity entity) {
		if (entity.equals(shipEntity))
			shipEntity = null;
	}
}
