package shared.components;

import hecs.Entity;
import hecs.EntityComponent;
import hecs.EntityContainer;

public class ProjectileComponent extends EntityComponent implements EntityContainer {
	private float length;
	private Entity shipEntity;
	private float damage;
	private byte variation;
	private float elapsed;

	public ProjectileComponent(Entity shipEntity, byte variation, float length, float damage) {
		if (shipEntity != null) {
			this.shipEntity = shipEntity;
			shipEntity.attach(this);
		}
		this.variation = variation;
		this.length = length;
		this.damage = damage;
	}

	@Override
	protected void removeComponent() {
	}

	public byte getVariation() {
		return variation;
	}

	public float getDamage() {
		return damage;
	}

	public boolean hasElapsed() {
		return elapsed > length;
	}

	public float remaining() {
		return length - elapsed;
	}

	public Entity getShipEntity() {
		return shipEntity;
	}

	@Override
	public void detach(Entity entity) {
		if (entity.equals(shipEntity))
			shipEntity = null;
	}

	public void update(float dt) {
		elapsed += dt;
	}

}
