package particles;

import org.joml.Vector3f;

import models.Texture;

public class ParticleSystem {
	private float pps;
	private float speed;
	private float lifeLength;

	private Texture texture;
	private int texturePage;

	public ParticleSystem(float pps, float speed, float lifeLength, Texture texture, int texturePage) {
		this.pps = pps;
		this.speed = speed;
		this.lifeLength = lifeLength;
		this.texture = texture;
		this.texturePage = texturePage;
	}

	public void generateParticles(Vector3f systemCenter, float delta) {
		float particlesToCreate = pps * delta;
		int count = (int) Math.floor(particlesToCreate);
		float partialParticle = particlesToCreate % 1;
		for (int i = 0; i < count; i++) {
			emitParticle(systemCenter);
		}
		if (Math.random() < partialParticle) {
			emitParticle(systemCenter);
		}
	}

	private void emitParticle(Vector3f center) {
		float dirX = (float) Math.random() * 2f - 1f;
		float dirZ = (float) Math.random() * 2f - 1f;
		float dirY = (float) Math.random() * 2f - 1f;
		Vector3f velocity = new Vector3f(dirX, dirY, dirZ);
		velocity.normalize();
		velocity.mul(speed);
		new Particle(new Vector3f(center), velocity, lifeLength, (float) (Math.random() * 360), 1, texture, texturePage);
	}
}
