package particles;

import org.joml.Vector2f;
import org.joml.Vector3f;

import display.DisplayManager;
import entities.Camera;
import models.Texture;

public class Particle {
	private Vector3f position;
	private Vector3f velocity;
	private float lifeLength;
	private float rotation;
	private float scale;

	private float elapsedTime;
	private float distanceToCamera;

	private Texture texture;

	private Vector2f texturePageOffset = new Vector2f();
	private int texturePage;
	private Vector2f textureOffset1 = new Vector2f();
	private Vector2f textureOffset2 = new Vector2f();
	private float blendFactor;

	// Reuse this vector instead of creating a new one for every calculation.
	private static Vector3f tempVector = new Vector3f();

	public Particle(Vector3f position, Vector3f velocity, float lifeLength, float rotation, float scale, Texture texture, int texturePage) {
		super();
		this.position = position;
		this.velocity = velocity;
		this.lifeLength = lifeLength;
		this.rotation = rotation;
		this.scale = scale;
		this.texture = texture;
		this.texturePage = texturePage;
		texturePageOffset = new Vector2f(calculateTexturePageXOffset(), calculateTexturePageYOffset());

		ParticleManager.addParticle(this);
	}

	protected boolean update(Camera camera, float delta) {
		position.add(velocity.mul(delta, tempVector));
		elapsedTime += delta;
		distanceToCamera = camera.getPosition().sub(position, tempVector).lengthSquared();
		updateTextureCoordinate();
		return elapsedTime < lifeLength;
	}

	private float calculateTexturePageXOffset() {
		int column = texturePage % texture.getAtlasSize();
		return (float) column / (float) texture.getAtlasSize();
	}

	private float calculateTexturePageYOffset() {
		int row = texturePage / texture.getAtlasSize();
		return (float) row / (float) texture.getAtlasSize();
	}

	// Calculate what texture coordinate in the sub-texture of the particle texture that should be used depending on elapsed time.
	private void updateTextureCoordinate() {
		float lifeFactor = elapsedTime / lifeLength;
		int stageCount = texture.getAtlasSize() * texture.getAtlasSize();
		float atlasProgression = lifeFactor * stageCount;
		int index1 = (int) Math.floor(atlasProgression);
		int index2 = index1 < stageCount - 1 ? index1 + 1 : index1;
		blendFactor = atlasProgression % 1;
		System.out.println(stageCount);
		System.out.println("BLENDBLEND");
		setTextureOffset(textureOffset1, index1);
		setTextureOffset(textureOffset2, index2);
	}

	private void setTextureOffset(Vector2f offset, int index) {
		int column = index % texture.getAtlasSize();
		int row = index / texture.getAtlasSize();
		offset.x = (float) column / texture.getAtlasSize() / texture.getTexturPages() + texturePageOffset.x;
		offset.y = (float) row / texture.getAtlasSize() / texture.getTexturPages() + texturePageOffset.y;
		System.out.println(index + "   " + offset.x + " " + offset.y);
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

	public Texture getTexture() {
		return texture;
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
}
