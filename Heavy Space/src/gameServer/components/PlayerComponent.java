package gameServer.components;

import hecs.Entity;
import hecs.EntityComponent;
import hecs.EntityContainer;

public class PlayerComponent extends EntityComponent implements EntityContainer {
	private int playerID;
	private Entity ship;
	private boolean requestSpawnShip;

	public PlayerComponent(int playerID) {
		this.playerID = playerID;
	}

	@Override
	public void detach(Entity entity) {
		if (ship.equals(entity))
			ship = null;
	}

	@Override
	public void removeComponent() {
		ship = null;
	}

	public Entity getShip() {
		return ship;
	}

	public void controlShip(Entity ship) {
		this.ship = ship;
		ship.attach(this);
	}

	public void requestSpawnShip() {
		requestSpawnShip = true;
	}

	public boolean isRequestingSpawnShip() {
		return requestSpawnShip;
	}

	public void resetRequests() {
		requestSpawnShip = false;
	}

}
