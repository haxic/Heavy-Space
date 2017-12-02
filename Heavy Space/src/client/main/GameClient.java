package client.main;

import client.display.DisplayManager;
import client.gameData.GameModelLoader;
import client.renderers.RenderManager;
import hecs.EntityManager;
import shared.Config;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.Globals;
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
		gameFactory = new GameFactory(entityManager, gameModelLoader);
		eventHandler = new EventHandler();
		connectionManager = new ConnectionManager(eventHandler, "localhost", config.gameClientDefaultPort, config);
		renderManager = new RenderManager(entityManager, displayManager, loader, gameModelLoader.particleAtlasTexture);

		menuController = new MenuController(entityManager, eventHandler, gameFactory, config);
		currentController = menuController;

		loop();
	}

	public void handleEvents() {
		connectionManager.handleUDPRequests();
		connectionManager.handleTCPRequests();
		Event event;
		while ((event = eventHandler.poll()) != null) {
			switch (event.type) {
			case CLIENT_EVENT_AUTHENTICATE:
				String username = (String) event.data[0];
				String password = (String) event.data[1];
				// gameController.authenticate
				break;
			case CLIENT_EVENT_SERVER_JOIN:
				String ip = (String) event.data[0];
				int port = (int) event.data[1];
				if (gameController != null)
					gameController.close();
				if (connectionManager.joinServer(ip, port)) {
					gameController = new GameController(entityManager, eventHandler, gameFactory);
					currentController = gameController;
				}
				break;
			case CLIENT_EVENT_SERVER_DISCONNECT:
				if (gameController != null)
					gameController.close();
				connectionManager.disconnect();
				currentController = menuController;
				gameController = null;
				break;
			case CLIENT_EVENT_SERVER_FAILED_TO_CONNECT:
				currentController = menuController;
				gameController = null;
				System.out.println(event.type + ": " + event.data[0]);
				break;
			case CLIENT_EVENT_CREATE_UNIT:
				if (gameController != null)
					gameController.createUnitFromEvent(event);
				break;
			case CLIENT_EVENT_UPDATE_UNIT:
				if (gameController != null)
					gameController.updateUnitFromEvent(event);
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
			Globals.now = System.currentTimeMillis();
			currentController.processInputs();
			handleEvents();
			currentController.update();
			renderManager.render(currentController.getScene());
			displayManager.updateDisplay();
			frames++;
			if (Globals.now - timer >= 500) {
				connectionManager.ping();
				timer += 500;
//				System.out.println("TCP/UDP ping: " + tcpPinger.toString() + " " + udpPinger.toString());
				// System.out.println("Fps: " + frames + ". Entities:" +
				// entityManager.numberOfEntities() + ". Components:" +
				// entityManager.numberOfComponents() + ".");
				frames = 0;
			}
		}
		currentController.close();
		renderManager.cleanUp();
		connectionManager.disconnect();
		System.out.println("Client shutdown!");
	}

}
