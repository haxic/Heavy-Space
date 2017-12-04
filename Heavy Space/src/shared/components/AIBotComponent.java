package shared.components;

import hecs.EntityComponent;

public class AIBotComponent extends EntityComponent {

	public float acceleration;
	public float maxSpeed;
	public float direction;

	public AIBotComponent(float acceleration, float direction) {
		this.acceleration = acceleration;
		this.direction = direction;
		maxSpeed = acceleration * 2;
	}

	@Override
	protected void removeComponent() {
	}
}
