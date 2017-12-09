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
import gameServer.systems.SnapshotTransmitterClass;
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
	private SnapshotTransmitterClass snapshotTransmitterSystem;
	
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
		snapshotTransmitterSystem = new SnapshotTransmitterClass(entityManager, udpServer);
		initializeWorld();
		initializeServer();
		loop();
	}

	private void initializeWorld() {
//		 serverGameFactory.createBot(new Vector3f(600, 200, 0), 0.1f, 1);
//		 serverGameFactory.createBot(new Vector3f(200, -400, 0), 0.25f, 1);
//		 serverGameFactory.createBot(new Vector3f(-300, 700, 0), 0.5f, 1);
//		 serverGameFactory.createBot(new Vector3f(-500, -100, 0), 0.75f, 1);

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
		snapshotTransmitterSystem.process();
	}

}
