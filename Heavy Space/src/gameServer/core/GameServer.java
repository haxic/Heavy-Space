package gameServer.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

import gameServer.components.ClientComponent;
import gameServer.components.ClientGameDataTransferComponent;
import gameServer.components.ObstacleComponent;
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
import gameServer.systems.SnapshotTransmitterSystem;
import gameServer.systems.VisionSystem;
import hecs.Entity;
import hecs.EntityManager;
import hevent.EventManager;
import shared.components.DeathComponent;
import shared.components.MovementComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.components.ObjectComponent;
import shared.functionality.DataPacket;
import shared.functionality.EventHandler;
import shared.functionality.IntegerIdentifier;
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
	private ServerCommunicator serverCommunicator;
	private ValidationService validationService;
	private TCPServer tcpServer;
	private UDPServer udpServer;
	private UDPRequestHandler udpRequestHandler;

	private EntityManager entityManager;
	private EventManager eventManager;
	private ServerGameFactory serverGameFactory;

	private IntegerIdentifier tickIdentifier;

	private AIBotSystem aiBotSystem;
	private PlayerSystem playerSystem;
	private ShipSystem shipSystem;
	private MovementSystem movementSystem;
	private CollisionSystem collisionSystem;
	private ProjectileSystem projectileSystem;
	private RemoveSystem removeSystem;
	private SnapshotTransmitterSystem snapshotTransmitterSystem;

	private ServerConfig serverConfig;
	private VisionSystem visionSystem;

	public GameServer(ServerConfig serverConfig, String username, String password) {
		this.serverConfig = serverConfig;
		entityManager = new EntityManager();
		eventManager = new EventManager();
		playerManager = new PlayerManager(entityManager);
		serverGameFactory = new ServerGameFactory(entityManager, null);
		clientManager = new ClientManager(entityManager, playerManager);
		if (serverConfig.official) {
			serverCommunicator = new ServerCommunicator(serverConfig);
			serverCommunicator.authenticate(username, password);
		}
		validationService = new ValidationService(serverCommunicator, clientManager, 5000);
		tcpServer = new TCPServer(validationService);
		udpServer = new UDPServer();
		udpRequestHandler = new UDPRequestHandler(entityManager, eventManager, clientManager, udpServer);
		tickIdentifier = new IntegerIdentifier();

		aiBotSystem = new AIBotSystem(entityManager);
		playerSystem = new PlayerSystem(entityManager, eventManager, serverGameFactory);
		shipSystem = new ShipSystem(entityManager, eventManager, serverGameFactory);
		movementSystem = new MovementSystem(entityManager);
		collisionSystem = new CollisionSystem(entityManager);
		projectileSystem = new ProjectileSystem(entityManager);
		removeSystem = new RemoveSystem(entityManager);
		visionSystem = new VisionSystem(entityManager, eventManager);
		snapshotTransmitterSystem = new SnapshotTransmitterSystem(entityManager, udpServer);
		initializeWorld();
		initializeServer();
		loop();
	}

	private void initializeWorld() {
//		 serverGameFactory.createBot(new Vector3f(600, 200, 0), 0.1f, 1);
//		 serverGameFactory.createBot(new Vector3f(200, -400, 0), 0.25f, 1);
//		 serverGameFactory.createBot(new Vector3f(-300, 700, 0), 0.5f, 1);
//		 serverGameFactory.createBot(new Vector3f(-500, -100, 0), 0.75f, 1);

		System.out.println(serverGameFactory.createObstacle(new Vector3f(-1500, 0, 0)));
//		serverGameFactory.createObstacle(new Vector3f(-1500, 0, 0));
//		serverGameFactory.createObstacle(new Vector3f(1500, 0, 0));
//		serverGameFactory.createObstacle(new Vector3f(0, 1500, 0));
//		serverGameFactory.createObstacle(new Vector3f(0, -1500, 0));
//		serverGameFactory.createObstacle(new Vector3f(0, 0, 1500));
//		serverGameFactory.createObstacle(new Vector3f(0, 0, -1500));

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
		long logStart = start;
		long currentTime = start;
		long now;
		float dt;
		int tick = 0;
		boolean shouldStop = false;
		while (!shouldStop) {
			now = System.currentTimeMillis();
			if (now - start > timestep) {
				start += timestep;
				dt = timestepDT;
				currentTime = now;
				tick = tickIdentifier.get();
				tick(dt, tick, snapshotTransmitterSystem.getTick());
			}
			if (now - logStart > 1000) {
				logStart += 1000;
				int playerEntities = entityManager.sizeEntitiesContainingComponent(PlayerComponent.class);
				int clientEntities = entityManager.sizeEntitiesContainingComponent(ClientComponent.class);
				int shipEntities = entityManager.sizeEntitiesContainingComponent(ShipComponent.class);
				int projectileEntities = entityManager.sizeEntitiesContainingComponent(ProjectileComponent.class);
				int obstacleEntities = entityManager.sizeEntitiesContainingComponent(ObstacleComponent.class);
//				System.out.println("Tick: " + tick + ". SSTick total: " + snapshotTransmitterSystem.getTick()
//				+ ". Entities total: " + entityManager.getSize()
//				+ ". Players: " + playerManager.getSize() + ":" + playerEntities
//				+ ". Clients: " + clientManager.getSize() + ":" + clientEntities
//				+ ". Ships: " + shipEntities
//				+ ". Projectiles: " + projectileEntities
//				+ ". Obstacles: " + obstacleEntities
//				+ ".");
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void tick(float dt, int tick, short sstick) {
		processInputs(dt, sstick);
		processAI(dt);
		updateGameState(dt);
		sendSnapshot(tick);
	}

	private void processInputs(float dt, short sstick) {
		clientManager.process(dt, sstick);
		udpRequestHandler.process(dt, sstick);
	}

	private void processAI(float dt) {
		aiBotSystem.update(dt);
	}

	private void updateGameState(float dt) {
		removeSystem.process();
		playerSystem.process();
		shipSystem.process(dt);
		movementSystem.process(dt);
		collisionSystem.process();
		projectileSystem.process(dt);
		visionSystem.process();
	}

	private void sendSnapshot(int tick) {
		snapshotTransmitterSystem.process(tick);
	}

}
