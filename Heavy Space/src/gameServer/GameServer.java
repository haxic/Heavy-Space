package gameServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

import client.main.ServerGameFactory;
import gameServer.components.ClientComponent;
import gameServer.components.ClientGameDataTransferComponent;
import gameServer.network.IServerCommunicator;
import gameServer.network.ServerCommunicator;
import gameServer.network.TCPServer;
import gameServer.network.ValidationService;
import gameServer.systems.PlayerSystem;
import gameServer.systems.ShipSystem;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.MovementComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.components.ObjectComponent;
import shared.functionality.DataPacket;
import shared.functionality.EventHandler;
import shared.functionality.Globals;
import shared.functionality.ShortIdentifier;
import shared.functionality.network.RequestType;
import shared.functionality.network.UDPServer;
import shared.systems.AIBotSystem;
import shared.systems.CollisionSystem;
import shared.systems.MovementSystem;
import shared.systems.ProjectileSystem;

public class GameServer {

	private PlayerManager playerManager;
	private ClientManager clientManager;
	private IServerCommunicator serverCommunicator;
	private ValidationService validationService;
	private TCPServer tcpServer;
	private UDPServer udpServer;
	private UDPRequestHandler udpRequestHandler;
	private EventHandler eventHandler;

	private EntityManager entityManager;
	private ServerGameFactory serverGameFactory;

	private ShortIdentifier tickIdentifier;

	private AIBotSystem aiBotSystem;
	private PlayerSystem playerSystem;
	private ShipSystem shipSystem;
	private MovementSystem movementSystem;
	private CollisionSystem collisionSystem;
	private ProjectileSystem projectileSystem;

	private ServerConfig serverConfig;

	public GameServer(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
		entityManager = new EntityManager();
		eventHandler = new EventHandler();
		playerManager = new PlayerManager(entityManager);
		serverGameFactory = new ServerGameFactory(entityManager, null);
		clientManager = new ClientManager(entityManager, playerManager);
		if (serverConfig.official) {
			serverCommunicator = new ServerCommunicator(serverConfig);
			serverCommunicator.createAccount("testserver", "testserver");
			serverCommunicator.authenticate("testserver", "testserver");
		}
		validationService = new ValidationService(serverCommunicator, clientManager, 5000);
		tcpServer = new TCPServer(validationService);
		udpServer = new UDPServer();
		udpRequestHandler = new UDPRequestHandler(entityManager, clientManager, udpServer);
		tickIdentifier = new ShortIdentifier();
		aiBotSystem = new AIBotSystem(entityManager);
		playerSystem = new PlayerSystem(entityManager, serverGameFactory);
		shipSystem = new ShipSystem(entityManager, serverGameFactory);
		movementSystem = new MovementSystem(entityManager);
		collisionSystem = new CollisionSystem(entityManager);
		projectileSystem = new ProjectileSystem(entityManager);
		initializeWorld();
		initializeServer();
		loop();
	}

	private void initializeWorld() {
		// gameFactory.createBot(new Vector3f(0, -20, -10), new Vector3f(0, 0, -1), 100f);
		// gameFactory.createBot(new Vector3f(-20, 0, -20), new Vector3f(0, 0, 1), 60f);
		// gameFactory.createBot(new Vector3f(0, 0, 0), new Vector3f(0, 0, -1), 40f);
		// gameFactory.createBot(new Vector3f(0, 20, 10), new Vector3f(0, 0, 1), 60f);
		serverGameFactory.createBot(new Vector3f(20, 0, 0), 25, 1);
	}

	private void initializeServer() {
		try {
			tcpServer.startServer(serverConfig.ip, serverConfig.port);
			udpServer.startServer(serverConfig.ip, serverConfig.port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private final int timestep = 100;
	private final float timestepDT = timestep / 1000.0f;
	

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
		playerSystem.process();
		shipSystem.process();
		movementSystem.process();
		collisionSystem.process();
		projectileSystem.process();
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
			createUnits(dataPackets, cgdtComponent);
			updateUnits(dataPackets, cgdtComponent);
			cgdtComponent.clear();
			for (DataPacket dataPacket : dataPackets) {
				DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.getCurrentDataSize(), clientComponent.getUDPAddress(), clientComponent.getUDPPort());
				udpServer.sendData(datagramPacket);
			}
		}
	}

	private void updateUnits(List<DataPacket> dataPackets, ClientGameDataTransferComponent cgdtComponent) {
		List<Entity> updateUnits = entityManager.getEntitiesContainingComponent(ObjectComponent.class);
		if (updateUnits != null && !updateUnits.isEmpty())
			cgdtComponent.updateUnits(updateUnits);
		if (!cgdtComponent.getUpdateEntities().isEmpty()) {
			DataPacket dataPacket = createDataPacket((byte) RequestType.SERVER_REPONSE_UPDATE_ENTITIES.ordinal(), Globals.tick, (byte) dataPackets.size());

			byte entityCounter = 0;
			for (Entity updateEntity : cgdtComponent.getUpdateEntities()) {
				if (dataPacket.getCurrentDataSize() + 17 >= dataPacket.getMaxDataSize()) {
					dataPackets.add(closeDataPacket(entityCounter, dataPacket));

					// New data packet
					entityCounter = 0;
					dataPacket = createDataPacket((byte) RequestType.SERVER_REPONSE_UPDATE_ENTITIES.ordinal(), Globals.tick, (byte) dataPackets.size());
				}
				ObjectComponent unit = (ObjectComponent) entityManager.getComponentInEntity(updateEntity, ObjectComponent.class);
				ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(updateEntity, ProjectileComponent.class);
				if (projectile != null)
					continue;
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
	}

	private void createUnits(List<DataPacket> dataPackets, ClientGameDataTransferComponent cgdtComponent) {
		List<Entity> createUnits = entityManager.getEntitiesContainingComponent(SpawnComponent.class);
		if (createUnits != null && !createUnits.isEmpty())
			cgdtComponent.createUnits(createUnits);
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
				byte entityType = 0;
				ObjectComponent unit = (ObjectComponent) entityManager.getComponentInEntity(createEntity, ObjectComponent.class);
				ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(createEntity, ProjectileComponent.class);
				if (projectile != null)
					entityType = 1;
				byte entityVariation = 0;

				// 4 ints + 2 bytes = 18 bytes
				dataPacket.addInteger((int) (createEntity.getEID())); // 5, Entity id
				dataPacket.addByte(entityType); // 9, Entity type (ship, projectile, obstacle etc)
				dataPacket.addByte(entityVariation); // 10, Entity variation (what variation of the type)
				dataPacket.addInteger((int) (unit.getPosition().x * 1000)); // 11-14, Position x
				dataPacket.addInteger((int) (unit.getPosition().y * 1000)); // 15-18, Position y
				dataPacket.addInteger((int) (unit.getPosition().z * 1000)); // 19-22, Position z
				if (entityType == 1) {
					MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(createEntity, MovementComponent.class);
					dataPacket.addShort((short) (movement.getLinearVel().x)); // 23-24, Velocity x
					dataPacket.addShort((short) (movement.getLinearVel().y)); // 25-26, Velocity y
					dataPacket.addShort((short) (movement.getLinearVel().z)); // 27-28, Velocity z
				}
				entityCounter++;
			}
			dataPackets.add(closeDataPacket(entityCounter, dataPacket));
		}

		if (createUnits != null && !createUnits.isEmpty())
			for (Entity createdUnit : createUnits)
				entityManager.removeComponentAll(SpawnComponent.class, createdUnit);
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
