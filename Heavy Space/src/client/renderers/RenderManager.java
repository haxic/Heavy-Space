package client.renderers;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import client.components.LightComponent;
import client.display.DisplayManager;
import client.gameData.Camera;
import client.gameData.Particle;
import client.gameData.Scene;
import client.models.Model;
import client.models.Texture;
import hecs.Entity;
import hecs.EntityManager;
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
		Camera camera = scene.getCamera();
		Model skybox = scene.getSkybox();
		List<Particle> particles = scene.getParticleManager().getParticles();
		List<Entity> lights = scene.getLights();
		Map<Model, List<Entity>> actors = scene.getActors();
		boolean renderSolidParticles = scene.getParticleManager().isRenderSolidParticles();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.3f, 0, 0.3f, 1);
		camera.updateProjectionMatrix(displayManager.getAspectRatio());
		camera.updateViewMatrix();
		entityRenderer.render(scene.getEntityManager(), camera, lights, actors);
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
