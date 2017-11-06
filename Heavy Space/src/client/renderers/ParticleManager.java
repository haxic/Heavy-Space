package client.renderers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import client.entities.Camera;
import client.entities.Particle;
import client.gameData.ParticleSystem;
import utilities.InsertionSort;

public class ParticleManager {

	private List<Particle> particles = new ArrayList<Particle>();
	private List<ParticleSystem> particleSystems = new ArrayList<ParticleSystem>();
	private boolean renderSolidParticles;

	public List<Particle> getParticles() {
		return particles;
	}

	public void addParticleSystem(ParticleSystem particleSystem) {
		particleSystems.add(particleSystem);
	}

	public void removeParticleSystem(ParticleSystem particleSystem) {
		particleSystems.remove(particleSystem);
	}

	public void update(Camera camera, float dt) {
		for (ParticleSystem particleSystem : particleSystems) {
			particleSystem.update(particles, camera, dt);
		}
		Iterator<Particle> iterator = particles.iterator();
		while (iterator.hasNext()) {
			Particle particle = iterator.next();
			if (!particle.update(camera, dt)) {
				iterator.remove();
			}
		}
		if (renderSolidParticles)
			InsertionSort.sortHighToLow(particles);
	}

	public int size() {
		return particles.size();
	}

	public boolean isRenderSolidParticles() {
		return renderSolidParticles;
	}

	public void setRenderSolidParticles(boolean renderSolidParticles) {
		this.renderSolidParticles = renderSolidParticles;
	}

}
