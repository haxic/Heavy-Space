package gameServer;

import java.net.DatagramPacket;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import client.main.GameFactory;
import gameServer.components.ClientComponent;
import gameServer.components.PlayerComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.HealthComponent;
import shared.components.MovementComponent;
import shared.components.UnitComponent;
import shared.functionality.DataPacket;
import shared.functionality.Globals;
import shared.functionality.network.RequestType;
import shared.functionality.network.UDPServer;

public class UDPRequestHandler {

	private ClientManager clientManager;
	private UDPServer udpServer;
	private EntityManager entityManager;
	private GameFactory gameFactory;

	public UDPRequestHandler(EntityManager entityManager, GameFactory gameFactory, ClientManager clientManager, UDPServer udpServer) {
		this.entityManager = entityManager;
		this.gameFactory = gameFactory;
		this.clientManager = clientManager;
		this.udpServer = udpServer;
	}

	public void process() {
		DatagramPacket datagramPacket;
		while ((datagramPacket = udpServer.getData()) != null) {
			// Pretend that this is the message reader
			try {
				DataPacket dataPacket = new DataPacket(datagramPacket.getData());
				byte type = dataPacket.getByte();
				byte identifier = dataPacket.getByte(); // Request identifier, used for response
				RequestType requestType = RequestType.values()[type & 0xFF];
				switch (requestType) {
				case CLIENT_REQUEST_AUTHENTICATE_UDP: {
					if (true)
						System.out.println("CLIENT_REQUEST_AUTHENTICATE_UDP");
					String uuid = dataPacket.getString(32);
					boolean authenticated = clientManager.udpAuthenticationRequest(uuid, datagramPacket.getAddress(), datagramPacket.getPort());
					DatagramPacket sendDatagramPacket;
					DataPacket sendDataPacket = new DataPacket(new byte[6]);
					sendDataPacket.addByte(type); // 0, requestType
					sendDataPacket.addByte(identifier); // 1, Request identifier
					sendDataPacket.addShort(Globals.tick); // 2-3, Request identifier
					sendDataPacket.addByte((byte) 0); // 4, Response, 0 = not authenticated, 1 = authenticated
					sendDataPacket.addByte((byte) 20); // 5, End data packet
					if (authenticated) {
						sendDataPacket.setByteAt((byte) 1, 4);
						sendDatagramPacket = new DatagramPacket(sendDataPacket.getData(), sendDataPacket.getCurrentDataSize(), datagramPacket.getAddress(), datagramPacket.getPort());
					} else {
						sendDatagramPacket = new DatagramPacket(sendDataPacket.getData(), sendDataPacket.getCurrentDataSize(), datagramPacket.getAddress(), datagramPacket.getPort());
					}
					udpServer.sendData(sendDatagramPacket);
					break;
				}
				case CLIENT_REQUEST_GAME_ACTION_CONTROL_SHIP: {
					String uuid = dataPacket.getString(32);
					Entity client = clientManager.getClient(uuid);
					ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(client, ClientComponent.class);
					// TODO: tell client invalid uuid
					if (client == null || clientComponent == null)
						break;
					System.out.println("CLIENT_REQUEST_GAME_ACTION_CONTROL_SHIP");
					int intX = dataPacket.getInteger();
					int intY = dataPacket.getInteger();
					int intZ = dataPacket.getInteger();
					Vector3f newPosition = new Vector3f(intX / 1000.0f, intY / 1000.0f, intZ / 1000.0f);
					int intVelX = dataPacket.getShort();
					int intVelY = dataPacket.getShort();
					int intVelZ = dataPacket.getShort();
					Vector3f newVel = new Vector3f(intVelX / 1000.0f, intVelY / 1000.0f, intVelZ / 1000.0f);
					Entity player = clientComponent.getPlayer();
					controlShip(player, newPosition, newVel);
				}
					break;
				case CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP: {
					System.out.println("CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP");
					String uuid = dataPacket.getString(32);
					Entity client = clientManager.getClient(uuid);
					ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(client, ClientComponent.class);
					// TODO: tell client invalid uuid
					if (client == null || clientComponent == null)
						break;
					Entity player = clientComponent.getPlayer();
					createShip(player);
				}
					break;
				case CLIENT_REQUEST_PING: {
					String uuid = dataPacket.getString(32);
					Entity client = clientManager.getClient(uuid);
					ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(client, ClientComponent.class);
					// TODO: tell client invalid uuid
					if (client == null || clientComponent == null)
						break;
					DataPacket sendDataPacket = new DataPacket(new byte[6]);
					sendDataPacket.addByte((byte) RequestType.CLIENT_REQUEST_PING.ordinal()); // 0
					sendDataPacket.addByte((byte) identifier); // 1
					sendDataPacket.addShort(Globals.tick); // 2-3
					sendDataPacket.addByte((byte) 20); // 4
					clientComponent.sendData(sendDataPacket.getData());
					DatagramPacket sendDatagramPacket = new DatagramPacket(sendDataPacket.getData(), sendDataPacket.getCurrentDataSize(), datagramPacket.getAddress(), datagramPacket.getPort());
					udpServer.sendData(sendDatagramPacket);
					// System.out.println("SERVER UDP RECEIVED: " + RequestType.CLIENT_REQUEST_PING + " " + identifier);
				}
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void controlShip(Entity player, Vector3fc newPosition, Vector3fc newVel) {
		PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(player, PlayerComponent.class);
		Entity ship = playerComponent.getShip();
		// TODO: tell client that it cannot be done
		if (ship == null)
			return;
		HealthComponent healthComponent = (HealthComponent) entityManager.getComponentInEntity(ship, HealthComponent.class);
		// TODO: tell client that ship is destroyed
		if (healthComponent.coreIntegrity <= 0)
			return;
		UnitComponent unitComponent = (UnitComponent) entityManager.getComponentInEntity(ship, UnitComponent.class);
		MovementComponent movementComponent = (MovementComponent) entityManager.getComponentInEntity(ship, MovementComponent.class);
		unitComponent.getPosition().set(newPosition);
		movementComponent.getLinearVel().set(newVel);
	}

	private void createShip(Entity player) {
		PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(player, PlayerComponent.class);
		Entity ship = playerComponent.getShip();
		// If player has no ship yet, create a new one
		if (ship == null) {
			ship = gameFactory.createShip(new Vector3f(0, 0, 0));
			playerComponent.controlShip(ship);
		} else {
			HealthComponent healthComponent = (HealthComponent) entityManager.getComponentInEntity(ship, HealthComponent.class);
			// TODO: tell client that current ship is still functional
			if (healthComponent.coreIntegrity > 0)
				return;
			UnitComponent unitComponent = (UnitComponent) entityManager.getComponentInEntity(ship, UnitComponent.class);
			MovementComponent movementComponent = (MovementComponent) entityManager.getComponentInEntity(ship, MovementComponent.class);
			unitComponent.getPosition().set(new Vector3f(0, 0, 0));
			movementComponent.getLinearVel().set(new Vector3f(0, 0, 0));
			healthComponent.coreIntegrity = healthComponent.coreIntegrityMax;
		}
	}

}
