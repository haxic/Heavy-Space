package client.main;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.display.DisplayManager;
import client.inputs.KeyboardHandler;
import hecs.EntityManager;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;

public class GameController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private GameFactory gameFactory;

	private EntityManager entityManager;

	private static final int KEY_SPAWN = GLFW.GLFW_KEY_SPACE;

	public GameController(EntityManager entityManager, GameFactory gameFactory) {
		this.entityManager = entityManager;
		this.gameFactory = gameFactory;
		scene = new Scene(entityManager);
		gameFactory.setSkybox(scene);
	}

	float mouseSpeed = 0.25f;
	float speed = 50f;
	float currentSpeed = speed;
	float rollSpeed = 2f;
	int invertX = 1;
	int invertY = -1;

	@Override
	public void processInputs(float deltaTime) {
		Vector2f mousePositionDelta = DisplayManager.getMousePositionDelta();
		float hor = invertX * mouseSpeed * deltaTime * mousePositionDelta.x;
		float ver = invertY * mouseSpeed * deltaTime * mousePositionDelta.y;
		if (!DisplayManager.isCursorEnabled()) {
			scene.camera.pitch(ver);
			scene.camera.yaw(hor);
		}
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_SPACE)) {
			currentSpeed = speed * 4;
		}
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_W))
			scene.camera.position.add(scene.camera.getForward().mul(deltaTime * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_S))
			scene.camera.position.sub(scene.camera.getForward().mul(deltaTime * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_D))
			scene.camera.position.add(scene.camera.right.mul(deltaTime * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_A))
			scene.camera.position.sub(scene.camera.right.mul(deltaTime * currentSpeed, new Vector3f()));

		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_Q))
			scene.camera.roll(deltaTime * rollSpeed);
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_E))
			scene.camera.roll(-deltaTime * rollSpeed);

		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			scene.camera.position.add(scene.camera.up.mul(deltaTime * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_SHIFT))
			scene.camera.position.sub(scene.camera.up.mul(deltaTime * currentSpeed, new Vector3f()));
		
		if (KeyboardHandler.kb_keyDownOnce(KEY_SPAWN)) {
			eventHandler.addEvent(new Event(EventType.SERVER_REQUEST_SPAWN));
		}
	}

	@Override
	public void update(float deltaTime) {
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void close() {
	}

}
