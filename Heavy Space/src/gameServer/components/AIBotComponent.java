package gameServer.components;

import org.joml.Vector3f;

import hecs.EntityComponent;

public class AIBotComponent extends EntityComponent {

	public int elapsed;
	public Vector3f targetLocation = new Vector3f(40, 40, -40);
	public int timeLimit;
	public float acceleration;
	public Vector3f temp = new Vector3f();
	
	public AIBotComponent(int timeLimit, float acceleration) {
		this.timeLimit = timeLimit;
		this.acceleration = acceleration;
	}

	@Override
	protected void removeComponent() {
	}
}
