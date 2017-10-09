package entities;

import org.joml.Vector3f;

public class Entity {
	public static int counter = 0;
	private Vector3f position;
	private Vector3f rotation;
	private Vector3f scale;
	public int id;

	public Entity(Vector3f position, Vector3f rotation, Vector3f scale) {
		id = counter++;
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public void setRotation(Vector3f rotation) {
		this.rotation = rotation;
	}

	public Vector3f getScale() {
		return scale;
	}

	public void setScale(Vector3f scale) {
		this.scale = scale;
	}

}
