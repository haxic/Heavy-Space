package client.main;

import client.display.DisplayManager;
import client.gameData.GameModelLoader;
import client.renderers.RenderManager;
import hecs.EntityManager;
import shared.Config;
import shared.functionality.Event;
import shared.functionality.EventHandler;
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
	private EntityManager entityManager;
	private GameFactory gameFactory;

	public GameClient() {
		entityManager = new EntityManager();
		loader = new Loader();
		displayManager = new DisplayManager(1200, 800);
		gameModelLoader = new GameModelLoader(loader);
		LocalConfig config = new LocalConfig();
		gameFactory = new GameFactory(entityManager, gameModelLoader, false);
		eventHandler = new EventHandler();
		connectionManager = new ConnectionManager(eventHandler, "localhost", config.gameClientDefaultPort, config);
		renderManager = new RenderManager(entityManager, displayManager, loader, gameModelLoader.particleAtlasTexture);

		menuController = new MenuController(entityManager, eventHandler, gameFactory, config);
		currentController = menuController;

		loop();
	}

	public void handleEvents() {
		Event event;
		while ((event = eventHandler.poll()) != null) {
			switch (event.type) {
			case AUTHENTICATE:
				String username = (String) event.data[0];
				String password = (String) event.data[1];
				// gameController.authenticate
				break;
			case JOIN_SERVER:
				String ip = (String) event.data[0];
				int port = (int) event.data[1];
				if (gameController != null)
					gameController.close();
				connectionManager.disconnect();
				if (connectionManager.joinServer(ip, port))
					gameController = new GameController(entityManager, gameFactory);
				currentController = gameController;
				break;
			case DISCONNECT:
				if (gameController != null)
					gameController.close();
				connectionManager.disconnect();
				currentController = menuController;
				break;
			case JOIN_SERVER_FAILED:
				System.out.println(event.type + ": " + event.data[0]);
				break;
			default:
				break;
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
			currentController.processInputs(displayManager.getDeltaTime());
			handleEvents();
			currentController.update(displayManager.getDeltaTime());
			renderManager.render(currentController.getScene());
			displayManager.updateDisplay();
			frames++;
			if (System.currentTimeMillis() - timer >= 1000) {
				timer += 1000;
//				System.out.println("Fps: " + frames + ". Entities:" + entityManager.numberOfEntities() + ". Components:" + entityManager.numberOfComponents() + ".");
				frames = 0;
			}
		}
		currentController.close();
	}

}
