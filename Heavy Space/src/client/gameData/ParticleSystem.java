package client.gameData;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import client.entities.Camera;
import client.entities.Particle;
import client.models.Texture;

public class ParticleSystem {

	private int texturePage;
	private Vector2f texturePageOffset;
	private Vector3f position;

	int particleCounter;
	private float lifeLength;
	float cooldown;
	float timeCounter;
	private boolean removeThis;
	private Texture texture;

	public ParticleSystem(Texture texture, Vector3f position, int texturePage, float cooldown, float lifeLength) {
		this.texture = texture;
		this.position = position;
		this.texturePage = texturePage;
		this.lifeLength = lifeLength;
		this.cooldown = cooldown;
		texturePageOffset = new Vector2f(calculateTexturePageXOffset(), calculateTexturePageYOffset());
	}

	private float calculateTexturePageXOffset() {
		int column = texturePage % texture.getAtlasSize();
		return (float) column / (float) texture.getAtlasSize();
	}

	private float calculateTexturePageYOffset() {
		int row = texturePage / texture.getAtlasSize();
		return (float) row / (float) texture.getAtlasSize();
	}

	public void update(List<Particle> particles, Camera camera, float dt) {
		timeCounter += dt;
		if (timeCounter >= cooldown && !removeThis) {
			timeCounter -= cooldown;
			float dirX = (float) Math.random() * 2f - 1f;
			float dirZ = (float) Math.random() * 2f - 1f;
			float dirY = (float) Math.random() * 2f - 1f;
			Vector3f velocity = new Vector3f(dirX, dirY, dirZ);
			velocity.normalize();
			velocity.mul(1);
			particles.add(new Particle(this, new Vector3f(position), velocity, (float) (Math.random() * 360), 1, texturePage, false));
		}
	}

	private Vector3f tempVector = new Vector3f();
	
	public void setPosition(Vector3f position) {
		this.position.set(position);
	}

	public boolean update(Particle particle, Camera camera, float delta) {
		particle.getPosition().add(particle.getVelocity().mul(delta, tempVector));
		particle.setElapsedTime(particle.getElapsedTime() + delta);
		particle.setDistanceToCamera(camera.getPosition().sub(particle.getPosition(), tempVector).lengthSquared());
		updateTextureCoordinate(particle);
		return particle.getElapsedTime() < lifeLength;
	}

	private void updateTextureCoordinate(Particle particle) {
		float lifeFactor = particle.getElapsedTime() / lifeLength;
		int stageCount = texture.getAtlasSize() * texture.getAtlasSize();
		float atlasProgression = lifeFactor * stageCount;
		int index1 = (int) Math.floor(atlasProgression);
		int index2 = index1 < stageCount - 1 ? index1 + 1 : index1;
		particle.setBlendFactor(atlasProgression % 1);
		setTextureOffset(particle.getTextureOffset1(), index1);
		setTextureOffset(particle.getTextureOffset2(), index2);
	}

	private void setTextureOffset(Vector2f offset, int index) {
		int column = index % texture.getAtlasSize();
		int row = index / texture.getAtlasSize();
		offset.x = (float) column / texture.getAtlasSize() / texture.getTexturePages() + texturePageOffset.x;
		offset.y = (float) row / texture.getAtlasSize() / texture.getTexturePages() + texturePageOffset.y;
	}

}
