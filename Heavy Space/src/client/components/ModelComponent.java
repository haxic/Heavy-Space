package client.components;

import hecs.EntityComponent;

public class ModelComponent extends EntityComponent {
	int eeid;

	public ModelComponent(int eeid) {
		this.eeid = eeid;
	}

	public int getEEID() {
		return eeid;
	}

	@Override
	protected void removeComponent() {
	}
}
