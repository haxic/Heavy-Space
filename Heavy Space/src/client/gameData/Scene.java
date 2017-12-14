package client.gameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.components.ActorComponent;
import client.components.LightComponent;
import client.models.Model;
import hecs.Entity;
import hecs.EntityContainer;
import hecs.EntityManager;

public class Scene implements EntityContainer {

	private Camera camera;
	private List<Entity> lights;
	private Map<Model, List<Entity>> actors;
	private Model skybox;
	private ParticleManager particleManager;
	private EntityManager entityManager;

	public Scene(EntityManager entityManager) {
		this.entityManager = entityManager;
		camera = new Camera();
		lights = new ArrayList<Entity>();
		actors = new HashMap<Model, List<Entity>>();
		particleManager = new ParticleManager(entityManager);
	}

	public void update(float dt) {
		particleManager.update(camera, dt);
	}

	public void addParticleEntity(Entity entity) {
		particleManager.addEntity(entity);
	}

	public void removeParticleEntity(Entity entity) {
		particleManager.removeEntity(entity);
	}

	public void addLightEntity(Entity entity) {
		entity.attach(this);
		lights.add(entity);
	}
	
	public void removeLightEntity(Entity entity) {
		lights.remove(entity);
		entity.detach(this);
	}

	public void setSkybox(Model skybox) {
		this.skybox = skybox;
	}

	public void addActorEntity(Entity entity) {
		ActorComponent actorComponent = (ActorComponent) entityManager.getComponentInEntity(entity, ActorComponent.class);
		Model model = actorComponent.getModel();
		List<Entity> batch = actors.get(model);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<>();
			newBatch.add(entity);
			actors.put(model, newBatch);
		}
		entity.attach(this);
	}
	
	public void removeActorEntity(Entity entity) {
		detach(entity);
		entity.detach(this);
	}

	@Override
	public void detach(Entity entity) {
		ActorComponent actorComponent = (ActorComponent) entityManager.getComponentInEntity(entity, ActorComponent.class);
		if (actorComponent != null) {
			Model model = actorComponent.getModel();
			List<Entity> batch = actors.get(model);
			if (batch == null || batch != null && !batch.contains(entity)) {
				System.out.println("WARNING: trying to remove an entity that isn't in the rendering list! Entity id: " + entity.getEID() + ".");
				return;
			}
			batch.remove(entity);
			if (batch.isEmpty())
				actors.remove(model);
		}
		LightComponent lightComponent = (LightComponent) entityManager.getComponentInEntity(entity, LightComponent.class);
		if (lightComponent != null) {
			lights.remove(entity);
		}
	}
	
	public Camera getCamera() {
		return camera;
	}

	public List<Entity> getLights() {
		return lights;
	}

	public Map<Model, List<Entity>> getActors() {
		return actors;
	}

	public Model getSkybox() {
		return skybox;
	}

	public ParticleManager getParticleManager() {
		return particleManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}
}
