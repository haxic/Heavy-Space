package client.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import client.entities.Actor;
import client.entities.Camera;
import client.entities.Light;
import client.gameData.GameModelLoader;
import client.gameData.ParticleSystem;
import client.models.Model;
import client.renderers.ParticleManager;
import shared.game.Entity;

public class Scene {

	private GameModelLoader gameModelLoader;
	public Camera camera;
	public List<Light> lights;
	public Map<Model, List<Actor>> actors;
	public Model skybox;
	public ParticleManager particleManager;

	public Scene(GameModelLoader gameModelLoader) {
		this.gameModelLoader = gameModelLoader;
		camera = new Camera();
		lights = new ArrayList<Light>();
		actors = new HashMap<Model, List<Actor>>();
		particleManager = new ParticleManager();
	}

	public void update(float dt) {
		particleManager.update(camera, dt);
	}

	public void addActor(Actor actor) {
		Model model = actor.getModel();
		List<Actor> batch = actors.get(model);
		if (batch != null) {
			batch.add(actor);
		} else {
			List<Actor> newBatch = new ArrayList<>();
			newBatch.add(actor);
			actors.put(model, newBatch);
		}
	}

	public void removeActor(Actor actor) {
		Model model = actor.getModel();
		List<Actor> batch = actors.get(model);
		if (batch == null || batch != null && !batch.contains(actor)) {
			System.out.println("WARNING: trying to remove an actor that isn't in the rendering list! Actor id: " + actor + ".");
			return;
		}
		batch.remove(actor);
		if (batch.isEmpty())
			actors.remove(model);
	}

	public void addParticleSystem(ParticleSystem particleSystem) {
		particleManager.addParticleSystem(particleSystem);
	}

	public void removeParticleSystem(ParticleSystem particleSystem) {
		particleManager.removeParticleSystem(particleSystem);
	}

	public void createMenuScene() {
		Light sun = new Light(new Vector3f(10000, 10000, 10000), new Vector3f(1, 1, 0), new Vector3f(0, 0, 0));
		lights.add(sun);
		Actor dragonActor = new Actor(new Entity(new Vector3f(0, -5, -20), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.dragon);
		addActor(dragonActor);
		skybox = gameModelLoader.skybox;
	}

}
