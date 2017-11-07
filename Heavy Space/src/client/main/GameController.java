package client.main;

import org.lwjgl.glfw.GLFW;

import client.gameData.GameModelLoader;
import client.inputs.KeyboardHandler;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;

public class GameController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private GameModelLoader gameModelLoader;

	private static final int KEY_SPAWN = GLFW.GLFW_KEY_SPACE;

	public GameController(GameModelLoader gameModelLoader) {
		this.gameModelLoader = gameModelLoader;
		scene = new Scene(gameModelLoader);
		scene.skybox = gameModelLoader.skybox;
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
