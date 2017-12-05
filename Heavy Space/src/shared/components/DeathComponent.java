package shared.components;

import hecs.Entity;
import hecs.EntityComponent;
import hecs.EntityContainer;

public class DeathComponent extends EntityComponent implements EntityContainer {

	private Entity killingEntity;
	private short tick;

	public DeathComponent() {
	}

	public DeathComponent(Entity killingEntity) {
		this.killingEntity = killingEntity;
		killingEntity.attach(this);
	}

	public DeathComponent(short tick, Entity killingEntity) {
		this.killingEntity = killingEntity;
		killingEntity.attach(this);
		this.tick = tick;
	}

	public DeathComponent(short tick) {
		this.tick = tick;
	}

	@Override
	protected void removeComponent() {
		killingEntity = null;
	}

	public Entity getKillingEntity() {
		return killingEntity;
	}

	@Override
	public void detach(Entity entity) {
		if (killingEntity != null && killingEntity.equals(entity))
			killingEntity = null;
	}

	public short getTick() {
		return tick;
	}
}
