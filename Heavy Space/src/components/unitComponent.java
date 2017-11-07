package components;

import org.joml.Vector3f;

import hecs.EntityComponent;

public class unitComponent implements EntityComponent {
	private Vector3f position;
	private Vector3f rotation;
	private Vector3f scale;
	public int id;

	public unitComponent(Vector3f position, Vector3f rotation, Vector3f scale) {
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
