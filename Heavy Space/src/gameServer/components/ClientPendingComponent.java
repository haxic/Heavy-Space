package gameServer.components;

import hecs.EntityComponent;

public class ClientPendingComponent extends EntityComponent {

	private boolean validated;
	private float elapsed;
	private float length;

	public ClientPendingComponent(float length) {
		this.length = length;
	}

	@Override
	protected void removeComponent() {
		System.out.println("REMOVE ME");
	}

	public boolean isValidated() {
		return validated;
	}

	public void validate() {
		validated = true;
	}

	public void update(float dt) {
		elapsed += dt;
	}

	public boolean overDue() {
		return elapsed > length;
	}
}
