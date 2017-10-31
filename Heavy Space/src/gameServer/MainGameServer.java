package gameServer;

import shared.game.WorldBuilder;

public class MainGameServer {

	TCPServer tcpServer;
	GameModel gameModel;
	public MainGameServer() {
		gameModel = new GameModel();
		initializeWorld();
		initializeServer();
		loop();
	}

	private void initializeWorld() {
		WorldBuilder.createTestWorld(gameModel);
	}

	private void initializeServer() {
		setupTCPServer();
		setupUDPServer();
	}

	private void setupTCPServer() {
		tcpServer = new TCPServer();
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
				System.out.println(hz);
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
		processAI();
		updateGameState();
		sendGameSate();
	}

	private void sendGameSate() {
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
