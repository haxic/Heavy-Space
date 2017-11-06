package client.main;

import org.lwjgl.glfw.GLFW;

import client.gameData.GameModelLoader;
import client.inputs.KeyboardHandler;
import client.renderers.RenderManager;
import shared.Config;

public class MenuController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private GameModelLoader gameModelLoader;

	private Config config;

	private static final int KEY_DISCONNECT = GLFW.GLFW_KEY_ESCAPE;
	private static final int KEY_JOIN = GLFW.GLFW_KEY_SPACE;

	public MenuController(EventHandler eventHandler, GameModelLoader gameModelLoader, Config config) {
		this.eventHandler = eventHandler;
		this.gameModelLoader = gameModelLoader;
		this.config = config;
		scene = new Scene(gameModelLoader);
		scene.createMenuScene();
	}

	int counter;

	@Override
	public void processInputs() {
		if (KeyboardHandler.kb_keyDownOnce(KEY_JOIN)) {
			counter++;
			System.out.println(counter);
			eventHandler.addEvent(new Event(Event.JOIN_SERVER, "localhost", config.gameServerDefaultPort));
		}
		if (KeyboardHandler.kb_keyDownOnce(KEY_DISCONNECT)) {
			eventHandler.addEvent(new Event(Event.DISCONNECT));
		}
	}

	@Override
	public void update(float dt) {
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void close() {
	}

}
