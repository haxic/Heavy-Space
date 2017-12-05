package shared.components;

import hecs.EntityComponent;

public class SpawnComponent extends EntityComponent {

	private short tick;
	private boolean instant;

	public SpawnComponent(short tick) {
		this.tick = tick;
	}
	
	public SpawnComponent() {
		instant = true;
	}

	@Override
	protected void removeComponent() {
	}

	public short getTick() {
		return tick;
	}
	
	public boolean instant() {
		return instant;
	}
}
