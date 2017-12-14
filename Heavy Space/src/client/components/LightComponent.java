package client.components;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import hecs.EntityComponent;

public class LightComponent extends EntityComponent {
	private Vector3f color;
	private Vector3f attenuation;
	public Matrix4f lightMatrix;

	public LightComponent(Vector3f color, Vector3f attenuation) {
		this.setColor(color);
		this.setAttenuation(attenuation);
	}

	private void setAttenuation(Vector3f attenuation) {
		this.attenuation = attenuation;
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

	@Override
	protected void removeComponent() {
	}
}
