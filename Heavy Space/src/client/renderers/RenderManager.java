package client.renderers;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import client.display.DisplayManager;
import client.entities.Actor;
import client.entities.Camera;
import client.entities.Light;
import client.entities.Particle;
import client.gameData.ParticleSystem;
import client.main.Scene;
import client.models.Model;
import client.models.Texture;
import utilities.Loader;

public class RenderManager {
	EntityRenderer entityRenderer;
	SkyboxRenderer skyboxRenderer;
	ParticleRenderer particleRenderer;
	DisplayManager displayManager;

	Scene current;

	public RenderManager(DisplayManager displayManager, Loader loader, Texture particleAtlasTexture) {
		this.displayManager = displayManager;
		entityRenderer = new EntityRenderer();
		skyboxRenderer = new SkyboxRenderer();
		particleRenderer = new ParticleRenderer(loader, particleAtlasTexture);

		enableBackCulling();
	}

	public void render(Scene scene) {
		Camera camera = scene.camera;
		Model skybox = scene.skybox;
		List<Particle> particles = scene.particleManager.getParticles();
		List<Light> lights = scene.lights;
		Map<Model, List<Actor>> actors = scene.actors;
		boolean renderSolidParticles = scene.particleManager.isRenderSolidParticles();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.3f, 0, 0.3f, 1);
		camera.updateProjectionMatrix(displayManager.getAspectRatio());
		camera.updateViewMatrix();
		entityRenderer.render(camera, lights, actors);
		skyboxRenderer.render(camera, skybox);
		particleRenderer.render(particles, camera, renderSolidParticles);
	}

	public void cleanUp() {
		entityRenderer.cleanUp();
		skyboxRenderer.cleanUp();
		particleRenderer.cleanUp();
	}

	protected static void enableBackCulling() {
		// Enable face culling and cull back faces (Don't render the back side
		// of polygons!)
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	protected static void disableBackCulling() {
		// Disable back culling (Render the back side of polygons!)
		GL11.glDisable(GL11.GL_CULL_FACE);
	}



}
