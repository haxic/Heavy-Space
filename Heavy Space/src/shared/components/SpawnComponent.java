package shared.components;

import hecs.EntityComponent;

public class SpawnComponent extends EntityComponent {

	short tick;

	public SpawnComponent(short tick) {
		this.tick = tick;
	}

	@Override
	protected void removeComponent() {
	}

	public short getTick() {
		return tick;
	}
}
