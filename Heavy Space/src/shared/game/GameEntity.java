package shared.game;

import org.joml.Vector3f;

public class GameEntity {
	public Vector3f position = new Vector3f();
	public Vector3f velocity = new Vector3f();
	public Vector3f orientation = new Vector3f();
	public Vector3f rotationalVelocity = new Vector3f();
	public Vector3f scale = new Vector3f(1, 1, 1);
}
