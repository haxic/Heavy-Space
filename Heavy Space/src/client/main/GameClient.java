package client.main;

import client.display.DisplayManager;
import client.gameData.GameModelLoader;
import client.renderers.RenderManager;
import shared.Config;
import tests.LocalConfig;
import utilities.Loader;

public class GameClient {
	private DisplayManager displayManager;
	private Loader loader;
	private GameModelLoader gameModelLoader;
	private RenderManager renderManager;

	private ConnectionManager connectionManager;

	private Config config;
	private EventHandler eventHandler;

	private MenuController menuController;
	private GameController gameController;
	private ClientController currentController;

	public GameClient() {
		loader = new Loader();
		displayManager = new DisplayManager(1200, 800);
		gameModelLoader = new GameModelLoader(loader);
		LocalConfig config = new LocalConfig();

		eventHandler = new EventHandler();
		connectionManager = new ConnectionManager(eventHandler, "localhost", config.gameClientDefaultPort, config);
		renderManager = new RenderManager(displayManager, loader, gameModelLoader.particleAtlasTexture);

		menuController = new MenuController(eventHandler, gameModelLoader, config);
		currentController = menuController;

		loop();
	}

	public void handleEvents() {
		Event event;
		while ((event = eventHandler.poll()) != null) {
			if (event.type == Event.AUTHENTICATE) {
				String username = (String) event.data[0];
				String password = (String) event.data[1];
				// gameController.authenticate
			}
			if (event.type == Event.JOIN_SERVER) {
				String ip = (String) event.data[0];
				int port = (int) event.data[1];
				if (gameController != null)
					gameController.close();
				connectionManager.disconnect();
				gameController = connectionManager.joinServer(ip, port);
				currentController = gameController;
			}
			if (event.type == Event.DISCONNECT) {
				if (gameController != null)
					gameController.close();
				connectionManager.disconnect();
				currentController = menuController;
			}
			if (event.type == Event.JOIN_SERVER_FAILED) {
				System.out.println(event.type + ": " + event.data[0]);
			}
		}
	}

	private void loop() {
		long timer = System.currentTimeMillis();
		// long removeTimer = timer;
		// int removeId = 0;
		int frames = 0;
		while (!displayManager.shouldClose()) {
			displayManager.pollInputs();
			currentController.processInputs();
			handleEvents();
			currentController.update(displayManager.getDeltaTime());
			renderManager.render(currentController.getScene());
			displayManager.updateDisplay();
			frames++;
			if (System.currentTimeMillis() - timer >= 1000) {
				timer += 1000;
				System.out.println("Fps: " + frames);
				frames = 0;
			}
		}
		currentController.close();
	}

}
