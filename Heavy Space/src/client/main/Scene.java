package client.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.components.ActorComponent;
import client.entities.Camera;
import client.entities.Light;
import client.gameData.ParticleSystem;
import client.models.Model;
import client.renderers.ParticleManager;
import hecs.EntityContainer;
import hecs.Entity;
import hecs.EntityManager;
import hecs.EntitySystem;

public class Scene extends EntitySystem implements EntityContainer {

	public Camera camera;
	public List<Light> lights;
	public Map<Model, List<Entity>> actors;
	public Model skybox;
	public ParticleManager particleManager;

	public Scene(EntityManager hecsManager) {
		super(hecsManager);
		camera = new Camera();
		lights = new ArrayList<Light>();
		actors = new HashMap<Model, List<Entity>>();
		particleManager = new ParticleManager();
	}

	public void update(float dt) {
		particleManager.update(camera, dt);
	}

	public void addParticleSystem(ParticleSystem particleSystem) {
		particleManager.addParticleSystem(particleSystem);
	}

	public void removeParticleSystem(ParticleSystem particleSystem) {
		particleManager.removeParticleSystem(particleSystem);
	}

	public void addLight(Light light) {
		lights.add(light);
	}

	public void setSkybox(Model skybox) {
		this.skybox = skybox;
	}

	@Override
	public void attach(Entity entity) {
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

	@Override
	public void detach(Entity entity) {
		ActorComponent actorComponent = (ActorComponent) entityManager.getComponentInEntity(entity, ActorComponent.class);
		Model model = actorComponent.getModel();
		List<Entity> batch = actors.get(model);
		if (batch == null || batch != null && !batch.contains(entity)) {
			System.out.println("WARNING: trying to remove an entity that isn't in the rendering list! Entity id: " + entity.getEID() + ".");
			return;
		}
		batch.remove(entity);
		if (batch.isEmpty())
			actors.remove(model);
		entity.detach(this);
	}

	@Override
	public void cleanUp() {
	}
}
