package shared.components;

import hecs.EntityComponent;

public class HealthComponent extends EntityComponent {

	public float coreIntegrityMax = 1000;
	public float coreIntegrity = coreIntegrityMax;

	@Override
	protected void removeComponent() {
	}

}
