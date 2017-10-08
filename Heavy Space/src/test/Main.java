package test;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import display.DisplayManager;
import entities.Actor;
import entities.Entity;
import entities.Light;
import inputs.KeyboardHandler;
import models.Loader;
import models.Model;
import renderers.RenderManager;
import utilities.OBJFileLoader;

public class Main {

	public static void main(String[] args) {
		Loader loader = new Loader();
		DisplayManager displayManager = new DisplayManager(1200, 800);
		Model skybox = loader.loadSkybox("space", 500);
		;
		RenderManager renderManager = new RenderManager(displayManager, skybox);
		Camera camera = new Camera();
		Light light = new Light(new Vector3f(0, 50, 0), new Vector3f(1, 1, 1));

		Model model = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("stall")), loader.loadTexture("stallTexture"));
		Model model2 = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("dragon")), loader.loadTexture("dragon"));
		Model model3 = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("fern")), loader.loadTexture("fern"));
		model3.setHasTransparency(true);
		model3.setAllowBackLighting(true);

		List<Actor> actors = new ArrayList<Actor>();
		actors.add(new Actor(new Entity(new Vector3f(0, 20, 0), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f)), model));

		actors.add(new Actor(new Entity(new Vector3f(0, 0, -25), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), model));
		actors.add(new Actor(new Entity(new Vector3f(0, 0, 25), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), model));
		actors.add(new Actor(new Entity(new Vector3f(25, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), model));
		actors.add(new Actor(new Entity(new Vector3f(-25, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), model));
		actors.add(new Actor(new Entity(new Vector3f(-25, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), model));
		actors.add(new Actor(new Entity(new Vector3f(15, 0, -25), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), model2));
		actors.add(new Actor(new Entity(new Vector3f(0, 0, -15), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), model3));

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
			currentTimeMillis = System.currentTimeMillis();
			// Handle inputs

			float bounceFactor = (float) Math.cos(((currentTimeMillis % 6100.0) / 1000.0));
			if (bounceFactor < 0)
				bounceFactor = -bounceFactor;
			light.getPosition().set(0, bounceFactor * 20, 0);
			displayManager.handleInputs();
			float dt = displayManager.getDeltaTime();
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
			// Render scene
			renderManager.render(camera, light);
			// Update display (draw on display)s
			displayManager.updateDisplay();

			// CHECK FPS
			frames++;
			if (System.currentTimeMillis() - timer >= 1000) {
				timer += 1000;
				System.out.println("Fps: " + frames + ".");
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
