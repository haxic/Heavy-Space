package client.entities;

import org.joml.Vector2f;
import org.joml.Vector3f;

import client.display.DisplayManager;
import client.gameData.ParticleComponent;
import client.models.Texture;

public class Particle {
	private Vector3f position;
	private Vector3f velocity;
	private float rotation;
	private float scale;
	private boolean solid;
	private ParticleComponent particleSystem;

	private float distanceToCamera;
	private Vector2f texturePageOffset = new Vector2f();
	private Vector2f textureOffset1 = new Vector2f();
	private Vector2f textureOffset2 = new Vector2f();
	private float blendFactor;
	private boolean isDead;
	private float elapsedTime;

	// Reuse this vector instead of creating a new one for every calculation.
	private static Vector3f tempVector = new Vector3f();

	public Particle(ParticleComponent particleSystem, Vector3f position, Vector3f velocity, float rotation, float scale, int texturePage, boolean solid) {
		this.particleSystem = particleSystem;
		this.position = position;
		this.velocity = velocity;
		this.rotation = rotation;
		this.scale = scale;
		this.solid = solid;
		// texturePageOffset = new Vector2f(calculateTexturePageXOffset(), calculateTexturePageYOffset());
	}

	public boolean update(Camera camera, float delta) {
		return particleSystem.update(this, camera, delta);
	}

	public float getDistanceToCamera() {
		return distanceToCamera;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getRotation() {
		return rotation;
	}

	public float getScale() {
		return scale;
	}

	public Vector2f getTextureOffset1() {
		return textureOffset1;
	}

	public Vector2f getTextureOffset2() {
		return textureOffset2;
	}

	public float getBlendFactor() {
		return blendFactor;
	}

	public float getRows() {
		return 0;
	}

	public boolean isSolid() {
		return solid;
	}

	public boolean isDead() {
		return isDead;
	}

	public Vector3f getVelocity() {
		return velocity;
	}

	public void setDistanceToCamera(float distanceToCamera) {
		this.distanceToCamera = distanceToCamera;
	}

	public float getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(float elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public void setBlendFactor(float blendFactor) {
		this.blendFactor = blendFactor;
	}

}
