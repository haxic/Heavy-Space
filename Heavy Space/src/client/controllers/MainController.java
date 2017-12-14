package client.controllers;

import client.display.DisplayManager;
import client.gameData.ClientGameFactory;
import client.gameData.GameAssetLoader;
import client.network.ConnectionManager;
import client.network.GameServerData;
import client.renderers.RenderManager;
import hecs.EntityManager;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.Globals;
import utilities.Loader;

public class MainController {
	private DisplayManager displayManager;
	private Loader loader;
	private GameAssetLoader gameAssetLoader;
	private RenderManager renderManager;

	private ConnectionManager connectionManager;

	private EventHandler eventHandler;

	private MenuController menuController;
	private GameController gameController;
	private IController currentController;

	public MainController(GameServerData gameServerData) {
		loader = new Loader();
		displayManager = new DisplayManager(1200, 800);
		gameAssetLoader = new GameAssetLoader(loader);
		eventHandler = new EventHandler();
		connectionManager = new ConnectionManager(eventHandler);
		renderManager = new RenderManager(displayManager, loader, gameAssetLoader.particleAtlasTexture);

		
		menuController = new MenuController(eventHandler, gameServerData, gameAssetLoader);
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
				GameServerData gameServerData = (GameServerData) event.data[0];
				if (gameController != null)
					gameController.close();
				if (connectionManager.joinServer(gameServerData)) {
					gameController = new GameController(eventHandler, connectionManager, gameAssetLoader);
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
				break;
			case CLIENT_EVENT_CREATE_UNIT:
				if (gameController != null)
					gameController.createEntityFromEvent(event);
				break;
			case CLIENT_EVENT_UPDATE_UNIT:
				if (gameController != null)
					gameController.updateUnitFromEvent(event);
				break;
			case CLIENT_EVENT_UPDATE_SNAPSHOT:
				if (gameController != null)
					gameController.updateSnapshotFromEvent(event);
				break;
			case CLIENT_EVENT_SPAWN_PLAYER_SHIP:
				if (gameController != null)
					gameController.spawnPlayerShipFromEvent(event);
				break;
			default:
				break;
			}
		}
	}

	private void loop() {
		long fpsTimer = System.currentTimeMillis();
		long pingTimer = fpsTimer;
		// long removeTimer = timer;
		// int removeId = 0;
		int frames = 0;
		while (!displayManager.shouldClose()) {
			displayManager.pollInputs();
			Globals.now = System.currentTimeMillis();
			currentController.processInputs();
			handleEvents();
			currentController.update();
			currentController.getScene().update(Globals.dt);
			renderManager.render(currentController.getScene());
			displayManager.updateDisplay();
			frames++;
			if (Globals.now - pingTimer >= 500) {
				connectionManager.ping();
				pingTimer += 500;
			}
			if (Globals.now - fpsTimer >= 1000) {
				fpsTimer += 1000;
				System.out.println("Fps: " + frames);
				frames = 0;
			}
		}
		currentController.close();
		renderManager.cleanUp();
		connectionManager.disconnect();
		System.out.println("Client shutdown!");
	}

}
