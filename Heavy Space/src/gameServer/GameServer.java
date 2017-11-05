package gameServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Vector3f;

import gameServer.AgentManager.Agent;
import gameServer.network.IServerCommunicator;
import gameServer.network.ServerCommunicator;
import gameServer.network.TCPServer;
import gameServer.network.UDPServer;
import gameServer.network.ValidationService;
import shared.Config;
import shared.DataPacket;
import shared.game.WorldBuilder;

public class GameServer {

	private PlayerManager playerManager;
	private AgentManager agentManager;
	private IServerCommunicator serverCommunicator;
	private ValidationService validationService;
	private TCPServer tcpServer;
	private UDPServer udpServer;

	private GameModel gameModel;

	private Config config;
	private String serverIP;
	private int serverPort;

	public GameServer(Config config, String serverIP, int serverPort, boolean local) {
		this.config = config;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		playerManager = new PlayerManager();
		agentManager = new AgentManager(playerManager);
		if (!local) {
			serverCommunicator = new ServerCommunicator(config.authenticationServerIP);
			serverCommunicator.createAccount("testserver", "testserver");
			serverCommunicator.authenticate("testserver", "testserver");
		}
		validationService = new ValidationService(serverCommunicator, agentManager, 5000, local);
		tcpServer = new TCPServer(serverIP, serverPort, validationService);
		udpServer = new UDPServer(serverIP, serverPort);
		gameModel = new GameModel();
		initializeWorld();
		initializeServer();
		loop();
	}

	private void initializeWorld() {
		WorldBuilder.createTestWorld(gameModel);
	}

	private void initializeServer() {
		try {
			udpServer.startServer();
			tcpServer.startServer();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void loop() {
		int hz = 1000 / 60;
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

	private void processInputs() {
		byte[] data;
		while ((data = udpServer.getData()) != null) {
			// Pretend that this is the message reader
			DataPacket dataPacket = new DataPacket(data);
			int intX = dataPacket.getInteger();
			int intY = dataPacket.getInteger();
			int intZ = dataPacket.getInteger();
			Vector3f newPosition = new Vector3f(intX / 1000.0f, intY / 1000.0f, intZ / 1000.0f);
			// pretend that the id of the player was send, such that it can be
			// used in agenmangeter
			agentManager.handleReceivedData(newPosition);
		}
	}

	private void processAI() {
	}

	private void updateGameState() {
	}

	private void sendGameSate() {
		List<DataPacket> gameState = new ArrayList<DataPacket>();
		for (Entry<String, Player> pair : playerManager.players.entrySet()) {
			Player player = pair.getValue();
			DataPacket dataPacket = new DataPacket(new byte[200]);
			dataPacket.addInteger((int) (player.position.x * 1000));
			dataPacket.addInteger((int) (player.position.y * 1000));
			dataPacket.addInteger((int) (player.position.z * 1000));
			dataPacket.addByte((byte) 20);
			gameState.add(dataPacket);
		}
		for (Entry<String, Agent> pair : agentManager.agents.entrySet()) {
			Agent agent = pair.getValue();
			for (DataPacket dataPacket : gameState) {
				DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.getLength(), agent.getUDPAddress(), config.gameClientDefaultPort);
				udpServer.sendData(datagramPacket);
			}
		}

	}

}
