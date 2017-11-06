package client.main;

import client.gameData.GameModelLoader;
import client.renderers.RenderManager;
import gameServer.network.SocketHandler;
import gameServer.network.UDPServer;

public class GameController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private GameModelLoader gameModelLoader;

	public GameController() {
		scene = new Scene(gameModelLoader);
		scene.skybox = gameModelLoader.skybox;
	}

	@Override
	public void processInputs() {
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
