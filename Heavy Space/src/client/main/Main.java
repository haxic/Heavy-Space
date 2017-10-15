package client.main;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.display.DisplayManager;
import client.entities.Actor;
import client.entities.Camera;
import client.entities.Entity;
import client.entities.Light;
import client.gameData.GameModelLoader;
import client.gameData.ParticleSystem;
import client.inputs.KeyboardHandler;
import client.models.Model;
import client.renderers.RenderManager;
import utilities.Loader;

public class Main {

	public static void main(String[] args) {
		Loader loader = new Loader();
		DisplayManager displayManager = new DisplayManager(1200, 800);
		Model skybox = loader.loadSkybox("space", 500);
		GameModelLoader gameModelLoader = new GameModelLoader(loader);
		RenderManager renderManager = new RenderManager(displayManager, skybox, loader, gameModelLoader.particleAtlasTexture);
		Camera camera = new Camera();
		
		ParticleSystem plasmaParticleSystem = new ParticleSystem(gameModelLoader.particleAtlasTexture, new Vector3f(10, 0, -10), 0, 0.01f, 1);
		renderManager.addParticleSystem(plasmaParticleSystem);
		renderManager.addParticleSystem(new ParticleSystem(gameModelLoader.particleAtlasTexture, new Vector3f(20, 0, -10), 1, 0.1f, 3));
		List<Light> lights = new ArrayList<Light>();
		
		Light sun = new Light(new Vector3f(0, 1000, 10000), new Vector3f(1, 1, 0), new Vector3f(0, 0, 0));
		
		Light plasma1 = new Light(new Vector3f(10, 0, -10), new Vector3f(0, 1f, 1f), new Vector3f(0.01f, 1, 1));
		Light plasma2 = new Light(new Vector3f(20, 0, -10), new Vector3f(0, 1f, 1f), new Vector3f(0.01f, 1, 1));
		lights.add(sun);
		lights.add(plasma1);
		lights.add(plasma2);
		
		

		
		List<Actor> actors = new ArrayList<Actor>();
		// actors.add(new Actor(new Entity(new Vector3f(0, 20, 0), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(0, 0, -25), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(0, 0, 25), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(25, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(-25, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(-25, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));

		Actor dragonActor = new Actor(new Entity(new Vector3f(0, 0, -20), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.dragon);
		actors.add(dragonActor);
		Actor fernActor = new Actor(new Entity(new Vector3f(10, 0, -15), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.fern);
		actors.add(fernActor);
		Actor fernActor2 = new Actor(new Entity(new Vector3f(-10, 0, -15), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.fern, 3);
		actors.add(fernActor2);

		for (Actor actor : actors) {
			renderManager.addActor(actor);
		}

		long currentTimeMillis;
		long timer = System.currentTimeMillis();
		// long removeTimer = timer;
		// int removeId = 0;
		int frames = 0;
		float mouseSpeed = 0.25f;
		float speed = 25f;
		float rollSpeed = 2f;
		int invertX = 1;
		int invertY = -1;
		while (!displayManager.shouldClose()) {
			float dt = displayManager.getDeltaTime();
			currentTimeMillis = System.currentTimeMillis();
			// Handle inputs

			displayManager.handleInputs();
			float hor = invertX * mouseSpeed * dt * (float) (displayManager.getWidth() / 2 - displayManager.getMouseX());
			float ver = invertY * mouseSpeed * dt * (float) (displayManager.getHeight() / 2 - displayManager.getMouseY());
			if (!displayManager.isCursorEnabled()) {
				camera.pitch(ver);
				camera.yaw(hor);
			}
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_W))
				camera.position.add(camera.getDirection().mul(dt * speed, new Vector3f()));
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_S))
				camera.position.sub(camera.getDirection().mul(dt * speed, new Vector3f()));
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_D))
				camera.position.add(camera.right.mul(dt * speed, new Vector3f()));
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_A))
				camera.position.sub(camera.right.mul(dt * speed, new Vector3f()));

			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_Q))
				camera.roll(dt * rollSpeed);
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_E))
				camera.roll(-dt * rollSpeed);

			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
				camera.position.add(camera.up.mul(dt * speed, new Vector3f()));
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_SHIFT))
				camera.position.sub(camera.up.mul(dt * speed, new Vector3f()));

			if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_LEFT_ALT))
				displayManager.toggleCursor();
			if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_Z))
				camera.toggleLockUp();

			// Logic
			float bounceFactor = (float) Math.cos(((currentTimeMillis % 6100.0) / 1000.0));
			if (bounceFactor < 0)
				bounceFactor = -bounceFactor;
//			dragonActor.getEntity().getRotation().y += 0.5f;
			// fernActor.getEntity().getRotation().y += 0.5f;
			plasma1.getPosition().set(0, bounceFactor * 20, -20);
			plasmaParticleSystem.setPosition(plasma1.getPosition());

			renderManager.update(camera, dt);

			// Render scene
			renderManager.render(camera, lights);

			// Update display (draw on display)s
			displayManager.updateDisplay();

			// CHECK FPS
			frames++;
			if (System.currentTimeMillis() - timer >= 1000) {
				timer += 1000;
				System.out.println("Fps: " + frames + "." + " " +renderManager.particleManager.size());
				frames = 0;
			}
			// REMOVE ENTITIES PERIODICALLY - for testing purposes
			// if (System.currentTimeMillis() - removeTimer >= 5000 && removeId < actors.size()) {
			// System.out.println("ACTOR REMOVED");
			// renderManager.removeActor(actors.get(removeId++));
			// removeTimer += 2000;
			// }
		}

		renderManager.cleanUp();
		displayManager.closeDisplay();
		loader.cleanUp();
	}

}
