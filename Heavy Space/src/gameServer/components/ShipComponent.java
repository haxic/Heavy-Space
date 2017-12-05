package gameServer.components;

import org.joml.Vector3f;

import hecs.Entity;
import hecs.EntityComponent;
import hecs.EntityContainer;

public class ShipComponent extends EntityComponent implements EntityContainer {
	private boolean requestFirePrimary;
	private Vector3f linearThrust;
	private Entity player;
	private boolean requestFireSecondary;

	public ShipComponent(Entity player) {
		this.player = player;
		player.attach(this);
		linearThrust = new Vector3f();
	}

	public ShipComponent() {
		linearThrust = new Vector3f();
	}

	@Override
	public void removeComponent() {
	}

	public Vector3f getLinearThrust() {
		return linearThrust;
	}

	public void update(float dt) {
		System.out.println(primaryRecharge + " " + secondaryRecharge);
		if (primaryRecharge > 0)
			primaryRecharge -= dt;
		if (secondaryRecharge > 0)
			secondaryRecharge -= dt;
	}

	// PRIMARY
	float primaryCooldown = 0.1f;
	float primaryRecharge;

	public void requestFirePrimary() {
		requestFirePrimary = true;
	}

	public boolean isRequestFirePrimary() {
		return requestFirePrimary && primaryRecharge <= 0;
	}

	public void firePrimary() {
		primaryRecharge += primaryCooldown;
	}

	// SECONDARY
	float secondaryCooldown = 0.4f;
	float secondaryRecharge;

	public void requestFireSecondary() {
		requestFireSecondary = true;
	}

	public boolean isRequestFireSecondary() {
		return requestFireSecondary && secondaryRecharge <= 0;
	}

	public void fireSecondary() {
		secondaryRecharge += secondaryCooldown;
	}

	// RESET ACTIONS
	public void resetRequests() {
		requestFirePrimary = false;
		requestFireSecondary = false;
		linearThrust.set(0);
	}

	@Override
	public void detach(Entity entity) {
		if (player.equals(entity))
			player = null;
	}

	public Entity getPlayer() {
		return player;
	}
}
