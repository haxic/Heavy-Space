package shared.components;

import hecs.EntityComponent;

public class HealthComponent extends EntityComponent {

	private float coreIntegrityMax = 1000;
	private float coreIntegrity = coreIntegrityMax;

	@Override
	protected void removeComponent() {
	}

	public float getCoreIntegrityMax() {
		return coreIntegrityMax;
	}

	public float getCoreIntegrity() {
		return coreIntegrity;
	}

	public void setCoreIntegrity(float coreIntegrity) {
		this.coreIntegrity = coreIntegrity;
	}
}
