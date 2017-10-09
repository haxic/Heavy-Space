package models;

import org.joml.Vector3f;

public class ModelAttachmentPoint {

	Vector3f position;
	Vector3f rotation;

	public ModelAttachmentPoint(Vector3f position, Vector3f rotation) {
		this.position = position;
		this.rotation = rotation;
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getRotation() {
		return rotation;
	}
}
