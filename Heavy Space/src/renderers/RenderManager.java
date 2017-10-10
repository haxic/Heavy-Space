package renderers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import display.DisplayManager;
import entities.Actor;
import entities.Camera;
import entities.Light;
import gameData.ParticleSystem;
import models.Model;
import models.Texture;
import utilities.Loader;

public class RenderManager {
	EntityRenderer entityRenderer;
	SkyboxRenderer skyboxRenderer;
	ParticleRenderer particleRenderer;
	DisplayManager displayManager;
	Map<Model, List<Actor>> actors;
	public ParticleManager particleManager;
	Model skybox;

	public RenderManager(DisplayManager displayManager, Model skybox, Loader loader, Texture particleAtlasTexture) {
		this.displayManager = displayManager;
		this.skybox = skybox;
		entityRenderer = new EntityRenderer();
		skyboxRenderer = new SkyboxRenderer();
		particleRenderer = new ParticleRenderer(loader, particleAtlasTexture);
		particleManager = new ParticleManager();
		actors = new HashMap<Model, List<Actor>>();
		enableBackCulling();
	}
	
	public void update(Camera camera, float delta) {
		particleManager.update(camera, delta);
	}

	public void render(Camera camera, Light light) {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.3f, 0, 0.3f, 1);
		camera.updateProjectionMatrix(displayManager.getAspectRatio());
		camera.updateViewMatrix();
		entityRenderer.render(camera, light, actors);
		skyboxRenderer.render(camera, skybox);
		particleRenderer.render(particleManager.getParticles(), camera, particleManager.isRenderSolidParticles());
	}

	public void cleanUp() {
		entityRenderer.cleanUp();
		skyboxRenderer.cleanUp();
		particleRenderer.cleanUp();
	}

	protected static void enableBackCulling() {
		// Enable face culling and cull back faces (Don't render the back side of polygons!)
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	protected static void disableBackCulling() {
		// Disable back culling (Render the back side of polygons!)
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public void addParticleSystem(ParticleSystem particleSystem) {
		particleManager.addParticleSystem(particleSystem);
	}
	
	public void removeParticleSystem(ParticleSystem particleSystem) {
		particleManager.removeParticleSystem(particleSystem);
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



}
