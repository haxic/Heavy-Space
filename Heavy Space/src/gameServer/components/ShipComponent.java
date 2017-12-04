package gameServer.components;

import org.joml.Vector3f;

import hecs.EntityComponent;

public class ShipComponent extends EntityComponent {
	private boolean requestFirePrimary;
	private Vector3f position;
	private Vector3f direction;

	public ShipComponent() {
		position = new Vector3f();
		direction = new Vector3f();
	}

	@Override
	public void removeComponent() {
	}

	public void requestFirePrimary() {
		requestFirePrimary = true;
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getDirection() {
		return direction;
	}

	public boolean isRequestFirePrimary() {
		return requestFirePrimary;
	}

	public void resetRequests() {
		requestFirePrimary = false;
	}
}
