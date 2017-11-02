package gameServer;

import gameServer.network.TCPServer;
import gameServer.network.ValidationService;
import shared.game.WorldBuilder;

public class MainGameServer {

	TCPServer tcpServer;
	GameModel gameModel;
	ValidationService validationService;

	public MainGameServer() {
		validationService = new ValidationService();
		tcpServer = new TCPServer(validationService);
		gameModel = new GameModel();
		initializeWorld();
		initializeServer();
		loop();
	}

	private void initializeWorld() {
		WorldBuilder.createTestWorld(gameModel);
	}

	private void initializeServer() {
		tcpServer.startServer();
		setupUDPServer();
	}

	private void setupUDPServer() {
	}

	private void loop() {
		int hz = 1000 / 100;
		long timer = System.currentTimeMillis();
		boolean shouldStop = false;
		while (!shouldStop) {
			if (System.currentTimeMillis() - timer > hz) {
				timer += hz;
				update();
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void update() {
		processInputs();
		// processAI();
		updateGameState();
		sendGameSate();
	}

	private void sendGameSate() {
		gameModel.players
	}

	private void updateGameState() {
	}

	private void processAI() {
	}

	private void processInputs() {
	}

	public static void main(String[] args) {
		new MainGameServer();
	}

}
