package gameServer.components;

import hecs.EntityComponent;
import shared.functionality.Globals;

public class ClientPendingComponent extends EntityComponent {
	
	private long timestamp;
	private boolean validated;

	public ClientPendingComponent() {
		timestamp = Globals.now;
	}

	@Override
	protected void removeComponent() {
		System.out.println("REMOVE ME");
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isValidated() {
		return validated;
	}

	public void validate() {
		validated = true;
		timestamp = Globals.now;
	}
}
