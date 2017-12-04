package gameServer;

import java.net.DatagramPacket;

import org.joml.Vector3f;
import gameServer.components.ClientComponent;
import gameServer.components.PlayerComponent;
import gameServer.components.ShipComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.functionality.DataPacket;
import shared.functionality.Globals;
import shared.functionality.network.RequestType;
import shared.functionality.network.UDPServer;
import utilities.BitConverter;

public class UDPRequestHandler {

	private ClientManager clientManager;
	private UDPServer udpServer;
	private EntityManager entityManager;

	public UDPRequestHandler(EntityManager entityManager, ClientManager clientManager, UDPServer udpServer) {
		this.entityManager = entityManager;
		this.clientManager = clientManager;
		this.udpServer = udpServer;
	}

	public void process() {
		DatagramPacket datagramPacket;
		while ((datagramPacket = udpServer.getData()) != null) {
			// Pretend that this is the message reader
			try {
				DataPacket dataPacket = new DataPacket(datagramPacket.getData());
				byte type = dataPacket.getByte(); // 0
				RequestType requestType = RequestType.values()[type & 0xFF];
				switch (requestType) {
				case CLIENT_REQUEST_AUTHENTICATE_UDP: {
					if (true)
						System.out.println("CLIENT_REQUEST_AUTHENTICATE_UDP");
					String uuid = dataPacket.getString(32); // 1-65
					byte identifier = dataPacket.getByte(); // 66, Request identifier - used for response
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
					String uuid = dataPacket.getString(32); // 1-64
					byte identifier = dataPacket.getByte(); // 65, Request identifier - used for response
					Entity client = clientManager.getClient(uuid);
					ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(client, ClientComponent.class);
					// TODO: tell client invalid uuid
					if (client == null || clientComponent == null)
						break;
					short tick = dataPacket.getShort(); // 66-67
					byte actionsBinary = dataPacket.getByte(); // 68
					boolean[] actions = BitConverter.booleanArrayFromByte(actionsBinary);
					int intX = dataPacket.getInteger();
					int intY = dataPacket.getInteger();
					int intZ = dataPacket.getInteger();
					Vector3f newPosition = new Vector3f(intX / 1000.0f, intY / 1000.0f, intZ / 1000.0f);
					short shortDirX = dataPacket.getShort();
					short shortDirY = dataPacket.getShort();
					short shortDirZ = dataPacket.getShort();
					Vector3f newDirection = new Vector3f(shortDirX / 100.0f, shortDirY / 100.0f, shortDirZ / 100.0f);
					Entity player = clientComponent.getPlayer();
					controlShip(player, actions, newPosition, newDirection);
				}
					break;
				case CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP: {
					System.out.println("CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP");
					String uuid = dataPacket.getString(32);
					byte identifier = dataPacket.getByte(); // 66, Request identifier - used for response
					Entity client = clientManager.getClient(uuid);
					// TODO: tell client invalid uuid
					if (client == null)
						break;
					ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(client, ClientComponent.class);
					if (clientComponent == null)
						break;
					Entity player = clientComponent.getPlayer();
					if (player == null)
						break;
					createShip(player);
				}
					break;
				case CLIENT_REQUEST_PING: {
					String uuid = dataPacket.getString(32);
					byte identifier = dataPacket.getByte(); // 66, Request identifier - used for response
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

	private void controlShip(Entity player, boolean[] actions, Vector3f newPosition, Vector3f newDirection) {
		PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(player, PlayerComponent.class);
		Entity shipEntity = playerComponent.getShip();
		if (shipEntity == null)
			return;
		ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(shipEntity, ShipComponent.class);
		// Fire primary
		if (actions[0])
			shipComponent.requestFirePrimary();
		try {
			shipComponent.getPosition().set(newPosition);
			if (newDirection.length() == 0)
				newDirection = new Vector3f(0, 0, 1);
			else
				newDirection.normalize();
			shipComponent.getDirection().set(newDirection);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void createShip(Entity player) {
		PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(player, PlayerComponent.class);
		playerComponent.requestSpawnShip();
	}

}
