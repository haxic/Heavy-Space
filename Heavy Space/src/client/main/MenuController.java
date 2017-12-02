package client.main;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.display.DisplayManager;
import client.entities.Light;
import client.inputs.KeyboardHandler;
import client.inputs.MousePositionHandler;
import client.network.GameServerData;
import gameServer.systems.AIBotSystem;
import hecs.Entity;
import hecs.EntityManager;
import shared.Config;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.functionality.Globals;

public class MenuController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private GameFactory gameFactory;
	private EntityManager entityManager;

	private GameServerData gameServerData;

	private static final int KEY_DISCONNECT = GLFW.GLFW_KEY_ESCAPE;
	private static final int KEY_JOIN = GLFW.GLFW_KEY_C;

	Entity dragon;

	private AIBotSystem aiBotSystem;

	public MenuController(EntityManager entityManager, EventHandler eventHandler, GameFactory gameFactory, GameServerData gameServerData) {
		this.entityManager = entityManager;
		this.eventHandler = eventHandler;
		this.gameFactory = gameFactory;
		this.gameServerData = gameServerData;

		aiBotSystem = new AIBotSystem(entityManager);

		scene = new Scene(entityManager);
		Light sun = new Light(new Vector3f(10000, 10000, 10000), new Vector3f(1, 1, 0), new Vector3f(0, 0, 0));
		scene.addLight(sun);
		gameFactory.setSkybox(scene);

		scene.addEntity(gameFactory.createBot(new Vector3f(0, -20, -10), new Vector3f(0, 0, -1), 10f));
		scene.addEntity(gameFactory.createBot(new Vector3f(-20, 0, -20), new Vector3f(0, 0, 1), 15f));
		scene.addEntity(gameFactory.createBot(new Vector3f(0, 0, 0), new Vector3f(0, 0, -1), 10f));
		scene.addEntity(gameFactory.createBot(new Vector3f(0, 20, 10), new Vector3f(0, 0, 1), 15f));
		scene.addEntity(gameFactory.createBot(new Vector3f(20, 0, 20), new Vector3f(0, 0, -1), 25f));
	}

	float mouseSpeed = 0.25f;
	float speed = 50f;
	float currentSpeed = speed;
	float rollSpeed = 2f;
	int invertX = 1;
	int invertY = -1;

	@Override
	public void processInputs() {
		Vector2f mousePositionDelta = DisplayManager.getMousePositionDelta();
		float hor = invertX * mouseSpeed * Globals.dt * mousePositionDelta.x;
		float ver = invertY * mouseSpeed * Globals.dt * mousePositionDelta.y;
		if (!DisplayManager.isCursorEnabled()) {
			scene.camera.pitch(ver);
			scene.camera.yaw(hor);
		}
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_SPACE)) {
			currentSpeed = speed * 4;
		}
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_W))
			scene.camera.position.add(scene.camera.getForward().mul(Globals.dt * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_S))
			scene.camera.position.sub(scene.camera.getForward().mul(Globals.dt * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_D))
			scene.camera.position.add(scene.camera.right.mul(Globals.dt * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_A))
			scene.camera.position.sub(scene.camera.right.mul(Globals.dt * currentSpeed, new Vector3f()));

		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_Q))
			scene.camera.roll(Globals.dt * rollSpeed);
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_E))
			scene.camera.roll(-Globals.dt * rollSpeed);

		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			scene.camera.position.add(scene.camera.up.mul(Globals.dt * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_SHIFT))
			scene.camera.position.sub(scene.camera.up.mul(Globals.dt * currentSpeed, new Vector3f()));

		// if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_LEFT_ALT))
		// DisplayManager.toggleCursor();
		if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_Z))
			scene.camera.toggleLockUp();

		if (KeyboardHandler.kb_keyDownOnce(KEY_JOIN)) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_JOIN, gameServerData));
		}
		if (KeyboardHandler.kb_keyDownOnce(KEY_DISCONNECT)) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_DISCONNECT));
		}
	}

	long timer = System.currentTimeMillis();
	boolean create;
	int frequency = 200;

	@Override
	public void update() {
		// if (System.currentTimeMillis() - timer >= frequency) {
		// timer += frequency;
		// if (create) {
		// dragon = gameFactory.createDragon();
		// scene.attach(dragon);
		// } else {
		// entityManager.removeEntity(dragon);
		// }
		// create = !create;
		// }
		aiBotSystem.update();
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void close() {
	}

}
