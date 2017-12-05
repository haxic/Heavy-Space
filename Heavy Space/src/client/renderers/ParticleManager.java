package client.renderers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Vector3f;

import client.entities.Camera;
import client.entities.Particle;
import client.gameData.ParticleComponent;
import hecs.Entity;
import hecs.EntityContainer;
import hecs.EntityManager;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import utilities.InsertionSort;

public class ParticleManager implements EntityContainer {

	private EntityManager entityManager;
	private List<Particle> particles = new ArrayList<Particle>();

	private List<Entity> particleEntities = new ArrayList<Entity>();

	private boolean renderSolidParticles;

	public ParticleManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public void addEntity(Entity entity) {
		entity.attach(this);
		particleEntities.add(entity);
	}

	public void removeEntity(Entity entity) {
		particleEntities.remove(entity);
		entity.detach(this);
	}

	public void update(Camera camera, float dt) {
		for (Entity entity : particleEntities) {
			ParticleComponent particleComponent = (ParticleComponent) entityManager.getComponentInEntity(entity, ParticleComponent.class);
			ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			MovementComponent movementComponent = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
			Vector3f position = new Vector3f(objectComponent.getPosition());
			Vector3f velocity = new Vector3f(movementComponent.getLinearVel());
			particleComponent.update(position, velocity, particles, camera, dt);
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

	@Override
	public void detach(Entity entity) {
		particleEntities.remove(entity);
	}

}
