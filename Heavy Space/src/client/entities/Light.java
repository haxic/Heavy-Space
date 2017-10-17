package client.entities;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Light {
	private Vector3f position;
	private Vector3f color;
	private Vector3f attenuation;
	public Matrix4f lightMatrix;

	public Light(Vector3f position, Vector3f color, Vector3f attenuation) {
		this.setPosition(position);
		this.setColor(color);
		this.setAttenuation(attenuation);
	}

	private void setAttenuation(Vector3f attenuation) {
		this.attenuation = attenuation;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getColor() {
		return color;
	}

	public void setColor(Vector3f color) {
		this.color = color;
	}

	public Vector3f getAttenuation() {
		return attenuation;
	}
}