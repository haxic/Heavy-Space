package gameServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

import gameServer.components.ClientComponent;
import gameServer.components.ClientGameDataTransferComponent;
import gameServer.components.PlayerComponent;
import gameServer.components.RemoveComponent;
import gameServer.components.ShipComponent;
import gameServer.network.IServerCommunicator;
import gameServer.network.ServerCommunicator;
import gameServer.network.TCPServer;
import gameServer.network.UDPRequestHandler;
import gameServer.network.ValidationService;
import gameServer.systems.RemoveSystem;
import gameServer.systems.PlayerSystem;
import gameServer.systems.ShipSystem;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.DeathComponent;
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
import utilities.BitConverter;

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
	private RemoveSystem removeSystem;

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
		removeSystem = new RemoveSystem(entityManager);
		initializeWorld();
		initializeServer();
		loop();
	}

	private void initializeWorld() {
//		 serverGameFactory.createBot(new Vector3f(600, 200, 0), 25, 1);
//		 serverGameFactory.createBot(new Vector3f(200, -400, 0), 50, 1);
//		 serverGameFactory.createBot(new Vector3f(-300, 700, 0), 75, 1);
//		 serverGameFactory.createBot(new Vector3f(-500, -100, 0), 100, 1);

		serverGameFactory.createObstacle(new Vector3f(-1500, 0, 0));
		serverGameFactory.createObstacle(new Vector3f(1500, 0, 0));
		serverGameFactory.createObstacle(new Vector3f(0, 1500, 0));
		serverGameFactory.createObstacle(new Vector3f(0, -1500, 0));
		serverGameFactory.createObstacle(new Vector3f(0, 0, 1500));
		serverGameFactory.createObstacle(new Vector3f(0, 0, -1500));

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

	private final int timestep = 15;
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
				Globals.tick = tickIdentifier.get();
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

	private void processInputs() {
		clientManager.process();
		udpRequestHandler.process();
	}

	private void processAI() {
		aiBotSystem.update();
	}

	private void updateGameState() {
		removeSystem.process();
		movementSystem.process(Globals.dt);
		playerSystem.process();
		shipSystem.process(Globals.dt);
		collisionSystem.process();
		projectileSystem.process(Globals.dt);
	}

	private void sendGameSate() {
		short nextSnapshot = (short) (Globals.tick / 3);
		if (nextSnapshot <= Globals.snapshotTick)
			return;
		Globals.snapshotTick = nextSnapshot;
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
			
			// Make sure that snapshot tick always gets sent
			if (dataPackets.isEmpty()) {
				DataPacket dataPacket = new DataPacket(new byte[4]);
				dataPacket.addByte((byte) RequestType.SERVER_REPONSE_UPDATE.ordinal()); // 0, Request type
				dataPacket.addShort(Globals.snapshotTick); // 1-2, Current game state
				dataPacket.addByte((byte) 20); // End of data
				dataPackets.add(dataPacket);
			} 

			cgdtComponent.clear();
			for (DataPacket dataPacket : dataPackets) {
				DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.getCurrentDataSize(), clientComponent.getUDPAddress(), clientComponent.getUDPPort());
				udpServer.sendData(datagramPacket);
			}
		}
	}

	boolean[] tempBools = new boolean[8];

	// UPDATE OBJECTS
	private void updateUnits(List<DataPacket> dataPackets, ClientGameDataTransferComponent cgdtComponent) {
		List<Entity> updateUnits = entityManager.getEntitiesContainingComponent(ObjectComponent.class);

		if (updateUnits != null && !updateUnits.isEmpty())
			cgdtComponent.updateUnits(updateUnits);
		if (!cgdtComponent.getUpdateEntities().isEmpty()) {
			DataPacket dataPacket = createDataPacket((byte) RequestType.SERVER_REPONSE_UPDATE_ENTITIES.ordinal(), Globals.snapshotTick, (byte) dataPackets.size());

			byte entityCounter = 0;
			for (Entity updateEntity : cgdtComponent.getUpdateEntities()) {
				ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(updateEntity, ProjectileComponent.class);
				MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(updateEntity, MovementComponent.class);
				DeathComponent death = (DeathComponent) entityManager.getComponentInEntity(updateEntity, DeathComponent.class);
				// Only update projectiles if they die
				if (projectile != null && death == null)
					continue;
				// Only update static objects if they die
				if (movement == null && death == null)
					continue;
				if (dataPacket.getCurrentDataSize() + 17 >= dataPacket.getMaxDataSize()) {
					dataPackets.add(closeDataPacket(entityCounter, dataPacket));

					// New data packet
					entityCounter = 0;
					dataPacket = createDataPacket((byte) RequestType.SERVER_REPONSE_UPDATE_ENTITIES.ordinal(), Globals.snapshotTick, (byte) dataPackets.size());
				}

				ObjectComponent unit = (ObjectComponent) entityManager.getComponentInEntity(updateEntity, ObjectComponent.class);

				if (death != null) {
					entityManager.addComponent(new RemoveComponent(), updateEntity);
				}

				tempBools[0] = movement != null && projectile == null;
				tempBools[1] = death != null ? true : false;
				tempBools[2] = death != null ? death.getKillingEntity() != null : false;

//				System.out.println("SEND ENTITY " + tempBools[0] + " " + updateEntity.getEID() + " " + unit.getPosition().x + ":" + unit.getPosition().y+ ":" + unit.getPosition().z);
				// 4 ints = 16 bytes
				dataPacket.addInteger((int) (updateEntity.getEID())); // 5-8, Entity id
				dataPacket.addByte(BitConverter.byteFromBooleanArray(tempBools)); // 9, Entity id
				if (tempBools[0]) {
					dataPacket.addInteger((int) (unit.getPosition().x * 1000)); // 10-13, Position x
					dataPacket.addInteger((int) (unit.getPosition().y * 1000)); // 14-17, Position y
					dataPacket.addInteger((int) (unit.getPosition().z * 1000)); // 18-21, Position z
					
					dataPacket.addShort((short) (unit.getForward().x * 100)); // 22-23, Forward x
					dataPacket.addShort((short) (unit.getForward().y * 100)); // 24-25, Forward y
					dataPacket.addShort((short) (unit.getForward().z * 100)); // 26-27, Forward z
					dataPacket.addShort((short) (unit.getUp().x * 100)); // 28-29, Up x
					dataPacket.addShort((short) (unit.getUp().y * 100)); // 30-31, Up y
					dataPacket.addShort((short) (unit.getUp().z * 100)); // 32-33, Up z
				}

				// If killed by another entity, add killing entity id
				if (tempBools[2]) {
					dataPacket.addInteger((int) (death.getKillingEntity().getEID())); // 22-25, Killing entity id
				}
				entityCounter++;
			}
			dataPackets.add(closeDataPacket(entityCounter, dataPacket));
		}
	}

	// UPDATE OBJECTS
	private void createUnits(List<DataPacket> dataPackets, ClientGameDataTransferComponent cgdtComponent) {
		List<Entity> createUnits = entityManager.getEntitiesContainingComponent(SpawnComponent.class);
		if (createUnits != null && !createUnits.isEmpty())
			cgdtComponent.createUnits(createUnits);
		if (!cgdtComponent.getCreateEntities().isEmpty()) {
			DataPacket dataPacket = createDataPacket(RequestType.SERVER_REPONSE_SPAWN_ENTITIES.asByte(), Globals.snapshotTick, (byte) dataPackets.size());

			byte entityCounter = 0;
			for (Entity createEntity : cgdtComponent.getCreateEntities()) {
				if (dataPacket.getCurrentDataSize() + 17 >= dataPacket.getMaxDataSize()) {
					dataPackets.add(closeDataPacket(entityCounter, dataPacket));

					// New data packet
					entityCounter = 0;
					dataPacket = createDataPacket(RequestType.SERVER_REPONSE_SPAWN_ENTITIES.asByte(), Globals.snapshotTick, (byte) dataPackets.size());
				}
				byte entityType;

				ProjectileComponent projectileComponent = (ProjectileComponent) entityManager.getComponentInEntity(createEntity, ProjectileComponent.class);
				ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(createEntity, ShipComponent.class);
				ObstacleComponent obstacleComponent = (ObstacleComponent) entityManager.getComponentInEntity(createEntity, ObstacleComponent.class);

				byte entityVariation = 0;
				short entityOwnerID = 0;

				if (shipComponent != null) {
					// SHIPS
					entityType = 0;
					PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(shipComponent.getPlayer(), PlayerComponent.class);
					if (playerComponent != null)
						entityOwnerID = playerComponent.getPlayerID();
				} else if (projectileComponent != null) {
					// PROJECTILES
					entityType = 1;
					entityVariation = projectileComponent.getVariation();
					entityOwnerID = (short) projectileComponent.getShipEntity().getEID();
				} else if (obstacleComponent != null) {
					// STATIC OBSTACLE
					entityType = 2;
				} else {
					entityType = 3;
				}

				ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(createEntity, ObjectComponent.class);

				// 4 ints + 2 bytes = 18 bytes
				dataPacket.addInteger((int) (createEntity.getEID())); // 5, Entity id
				dataPacket.addByte(entityType); // 9, Entity type (ship, projectile, obstacle etc)
				dataPacket.addByte(entityVariation); // 10, Entity variation (what variation of the type)
				if (entityType != 2)
					dataPacket.addShort(entityOwnerID); // 11-12, Player id
				dataPacket.addInteger((int) (objectComponent.getPosition().x * 1000)); // 13-16, Position x
				dataPacket.addInteger((int) (objectComponent.getPosition().y * 1000)); // 17-20, Position y
				dataPacket.addInteger((int) (objectComponent.getPosition().z * 1000)); // 21-24, Position z
				if (entityType == 1) {
					MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(createEntity, MovementComponent.class);
					dataPacket.addInteger((int) (movement.getLinearVel().x * 1000)); // 25-28, Velocity x
					dataPacket.addInteger((int) (movement.getLinearVel().y * 1000)); // 29-32, Velocity y
					dataPacket.addInteger((int) (movement.getLinearVel().z * 1000)); // 33-36, Velocity z
				}
				if (entityType != 1) {
					dataPacket.addShort((short) (objectComponent.getForward().x * 100)); // 25-26, Forward x
					dataPacket.addShort((short) (objectComponent.getForward().y * 100)); // 27-28, Forward y
					dataPacket.addShort((short) (objectComponent.getForward().z * 100)); // 29-30, Forward z
					dataPacket.addShort((short) (objectComponent.getUp().x * 100)); // 31-32, Up x
					dataPacket.addShort((short) (objectComponent.getUp().y * 100)); // 33-34, Up y
					dataPacket.addShort((short) (objectComponent.getUp().z * 100)); // 35-36, Up z
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
		if (numberOfEntities != 0)
			dataPacket.setByteAt(numberOfEntities, 4); // 3, Set number of entities.
		dataPacket.addByte((byte) 20); // End of data
		return dataPacket;
	}

}
