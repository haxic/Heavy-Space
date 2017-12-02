package gameServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.joml.Vector3f;
import client.main.GameFactory;
import gameServer.components.AIBotComponent;
import gameServer.components.ClientComponent;
import gameServer.components.ClientGameDataTransferComponent;
import gameServer.components.ClientValidatedComponent;
import gameServer.components.ClientPendingComponent;
import gameServer.network.IServerCommunicator;
import gameServer.network.ServerCommunicator;
import gameServer.network.TCPServer;
import gameServer.network.ValidationService;
import gameServer.systems.AIBotSystem;
import hecs.Entity;
import hecs.EntityManager;
import shared.Config;
import shared.DataPacket;
import shared.components.MovementComponent;
import shared.components.UnitComponent;
import shared.functionality.ByteIdentifier;
import shared.functionality.EventHandler;
import shared.functionality.Globals;
import shared.functionality.RequestType;
import shared.functionality.ShortIdentifier;
import shared.functionality.UDPServer;

public class GameServer {

	private PlayerManager playerManager;
	private ClientManager clientManager;
	private IServerCommunicator serverCommunicator;
	private ValidationService validationService;
	private TCPServer tcpServer;
	private UDPServer udpServer;
	private UDPRequestHandler udpRequestHandler;
	private EventHandler eventHandler;

	private Config config;
	private String serverIP;
	private int serverPort;
	private EntityManager entityManager;
	private GameFactory gameFactory;

	private ShortIdentifier tickIdentifier;

	private AIBotSystem aiBotSystem;

	public GameServer(Config config, String serverIP, int serverPort, boolean local) {
		this.config = config;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		entityManager = new EntityManager();
		eventHandler = new EventHandler();
		playerManager = new PlayerManager(entityManager);
		gameFactory = new GameFactory(entityManager, null);
		clientManager = new ClientManager(entityManager, playerManager);
		if (!local) {
			serverCommunicator = new ServerCommunicator(config.authenticationServerIP);
			serverCommunicator.createAccount("testserver", "testserver");
			serverCommunicator.authenticate("testserver", "testserver");
		}
		validationService = new ValidationService(serverCommunicator, clientManager, 5000, local);
		tcpServer = new TCPServer(serverIP, serverPort, validationService);
		udpServer = new UDPServer(serverIP, serverPort);
		udpRequestHandler = new UDPRequestHandler(entityManager, gameFactory, clientManager, udpServer);
		tickIdentifier = new ShortIdentifier();
		aiBotSystem = new AIBotSystem(entityManager);
		initializeWorld();
		initializeServer();
		loop();
	}

	private void initializeWorld() {
		gameFactory.createBot(new Vector3f(0, -20, -10), new Vector3f(0, 0, -1), 10f);
		gameFactory.createBot(new Vector3f(-20, 0, -20), new Vector3f(0, 0, 1), 15f);
		gameFactory.createBot(new Vector3f(0, 0, 0), new Vector3f(0, 0, -1), 10f);
		gameFactory.createBot(new Vector3f(0, 20, 10), new Vector3f(0, 0, 1), 15f);
		gameFactory.createBot(new Vector3f(20, 0, 20), new Vector3f(0, 0, -1), 25f);
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

	private final int timestep = 100;
	private final float timestepDT = 1.0f / timestep;

	private void loop() {
		long start = System.currentTimeMillis();
		Globals.now = start;
		long now;
		boolean shouldStop = false;
		while (!shouldStop) {
			now = System.currentTimeMillis();
			if (now - start > timestep) {
				start += timestep;
				Globals.dt = timestepDT;
				Globals.now = now;
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
		Globals.tick = tickIdentifier.get();
		processInputs();
		processAI();
		updateGameState();
		sendGameSate();
	}

	private void processInputs() {
		clientManager.process();
		udpRequestHandler.process();
	}

	private void processAI() {
		aiBotSystem.update();
	}

	private void updateGameState() {
	}

	private void sendGameSate() {

		List<Entity> clientEntities = entityManager.getEntitiesContainingComponent(ClientGameDataTransferComponent.class);
		if (clientEntities == null)
			return;
		if (clientEntities.isEmpty())
			return;

		for (Entity clientEntity : clientEntities) {
			ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(clientEntity, ClientComponent.class);
			if (clientComponent == null)
				continue;
			if (clientComponent.isDisconnected())
				continue;

			List<DataPacket> dataPackets = new ArrayList<DataPacket>();

			ClientGameDataTransferComponent cgdtComponent = (ClientGameDataTransferComponent) entityManager.getComponentInEntity(clientEntity, ClientGameDataTransferComponent.class);



			if (!cgdtComponent.getCreateEntities().isEmpty()) {
				DataPacket dataPacket = createDataPacket(RequestType.SERVER_REPONSE_SPAWN_ENTITIES.asByte(), Globals.tick, (byte) dataPackets.size());

				byte entityCounter = 0;
				for (Entity createEntity : cgdtComponent.getCreateEntities()) {

					if (dataPacket.getCurrentDataSize() + 17 >= dataPacket.getMaxDataSize()) {
						dataPackets.add(closeDataPacket(entityCounter, dataPacket));

						// New data packet
						entityCounter = 0;
						dataPacket = createDataPacket(RequestType.SERVER_REPONSE_SPAWN_ENTITIES.asByte(), Globals.tick, (byte) dataPackets.size());
					}
					UnitComponent unit = (UnitComponent) entityManager.getComponentInEntity(createEntity, UnitComponent.class);
					MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(createEntity, MovementComponent.class);

					// 4 ints + 2 bytes = 18 bytes
					dataPacket.addInteger((int) (createEntity.getEID())); // 4, Entity id
					dataPacket.addByte((byte) (0)); // 8, Entity type (obstacle, ship, projectile etc)
					dataPacket.addByte((byte) (0)); // 9, Entity variation (what variation of the type)
					dataPacket.addInteger((int) (unit.getPosition().x * 1000)); // 10-13, Position x
					dataPacket.addInteger((int) (unit.getPosition().y * 1000)); // 14-17, Position y
					dataPacket.addInteger((int) (unit.getPosition().z * 1000)); // 18-21, Position z
					entityCounter++;
				}
				dataPackets.add(closeDataPacket(entityCounter, dataPacket));
			}
			
			// ------------------- TEMPORARY CODE -------------------
			// ------------------- TEMPORARY CODE -------------------
			// ------------------- TEMPORARY CODE -------------------
			// ------------------- TEMPORARY CODE -------------------
			List<Entity> units = entityManager.getEntitiesContainingComponent(UnitComponent.class);
			cgdtComponent.updateUnits(units);
			if (!cgdtComponent.getUpdateEntities().isEmpty()) {
				DataPacket dataPacket = createDataPacket((byte) RequestType.SERVER_REPONSE_UPDATE_ENTITIES.ordinal(), Globals.tick, (byte) dataPackets.size());

				byte entityCounter = 0;
				System.out.println("UPDATE UNITS:");
				for (Entity updateEntity : cgdtComponent.getUpdateEntities()) {
					if (dataPacket.getCurrentDataSize() + 17 >= dataPacket.getMaxDataSize()) {
						dataPackets.add(closeDataPacket(entityCounter, dataPacket));

						// New data packet
						entityCounter = 0;
						dataPacket = createDataPacket((byte) RequestType.SERVER_REPONSE_UPDATE_ENTITIES.ordinal(), Globals.tick, (byte) dataPackets.size());
					}
					UnitComponent unit = (UnitComponent) entityManager.getComponentInEntity(updateEntity, UnitComponent.class);
					MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(updateEntity, MovementComponent.class);
					System.out.println(updateEntity.getEID());

					// 4 ints = 16 bytes
					dataPacket.addInteger((int) (updateEntity.getEID())); // 4-7, Entity id
					dataPacket.addInteger((int) (unit.getPosition().x * 1000)); // 8-11, Position x
					dataPacket.addInteger((int) (unit.getPosition().y * 1000)); // 12-15, Position y
					dataPacket.addInteger((int) (unit.getPosition().z * 1000)); // 16-19, Position z
					// System.out.println("SEND UPDATE " + (int) (unit.getPosition().z * 1000) + " " + unit.getPosition().z + " " + (int)
					// unit.getPosition().z);
					entityCounter++;
				}
				dataPackets.add(closeDataPacket(entityCounter, dataPacket));
			}

			cgdtComponent.clear();
			for (DataPacket dataPacket : dataPackets) {
				DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.getCurrentDataSize(), clientComponent.getUDPAddress(), clientComponent.getUDPPort());
				udpServer.sendData(datagramPacket);
			}
		}
	}

	private DataPacket createDataPacket(byte requestType, short currentTick, byte packetNumber) {
		DataPacket dataPacket = new DataPacket(new byte[508]);
		dataPacket.addByte(requestType); // 0, Request type
		dataPacket.addShort(currentTick); // 1-2, Current game state
		dataPacket.addByte(packetNumber); // 3, Packet number
		dataPacket.addByte((byte) 0); // 4, Number of entities. Value is set by calling closeDataPacket
		return dataPacket;
	}

	private DataPacket closeDataPacket(byte numberOfEntities, DataPacket dataPacket) {
		// Close data packet
		dataPacket.setByteAt(numberOfEntities, 4); // 3, Set number of entities.
		dataPacket.addByte((byte) 20); // End of data
		return dataPacket;
	}

}
