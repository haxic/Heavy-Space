package client.gameData;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import client.entities.Camera;
import client.entities.Particle;
import client.models.Texture;
import hecs.EntityComponent;

public class ParticleComponent extends EntityComponent {

	private int texturePage;
	private Vector2f texturePageOffset;

	int particleCounter;
	private float lifeLength;
	float cooldown;
	float timeCounter;
	private boolean removeThis;
	private Texture texture;
	private Vector3f tempVector = new Vector3f();
	private float scale;
	
	public ParticleComponent(Texture texture, int texturePage, float cooldown, float lifeLength, float scale) {
		this.texture = texture;
		this.texturePage = texturePage;
		this.lifeLength = lifeLength;
		this.cooldown = cooldown;
		this.scale = scale;
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

	public void update(Vector3f position, Vector3f velocity, List<Particle> particles, Camera camera, float dt) {
		timeCounter += dt;
		if (timeCounter >= cooldown && !removeThis) {
			timeCounter -= cooldown;
			float dirX = (float) Math.random() * 2f - 1f;
			float dirZ = (float) Math.random() * 2f - 1f;
			float dirY = (float) Math.random() * 2f - 1f;
			tempVector.set(dirX, dirY, dirZ).normalize().mul(10);
			velocity.mul(0.5f).add(tempVector);
			particles.add(new Particle(this, position, velocity, (float) (Math.random() * 360), scale, texturePage, false));
		}
	}

	public boolean update(Particle particle, Camera camera, float delta) {
		particle.getPosition().fma(delta, particle.getVelocity());
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

	@Override
	protected void removeComponent() {
	}

}
