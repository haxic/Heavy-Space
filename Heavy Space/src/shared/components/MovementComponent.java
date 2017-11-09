package shared.components;

import org.joml.Vector3f;

import hecs.EntityComponent;

public class MovementComponent implements EntityComponent {
	public Vector3f linearAcc = new Vector3f();
	public Vector3f linearVel = new Vector3f();
	
    public Vector3f angularAcc = new Vector3f();
	public Vector3f angularVel = new Vector3f();

	public MovementComponent() {
	}
}
