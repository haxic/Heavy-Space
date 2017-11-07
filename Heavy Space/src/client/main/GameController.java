package client.main;

import org.lwjgl.glfw.GLFW;

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

	@Override
	public void processInputs() {
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
