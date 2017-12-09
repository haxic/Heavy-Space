package gameServer.network;

import java.net.DatagramPacket;

import org.joml.Vector3f;

import gameServer.ClientManager;
import gameServer.components.ClientComponent;
import gameServer.components.PlayerComponent;
import gameServer.components.ShipComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
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
					String uuid = dataPacket.getString(32); // 1-65
					byte identifier = dataPacket.getByte(); // 66, Request identifier - used for response
					boolean authenticated = clientManager.udpAuthenticationRequest(uuid, datagramPacket.getAddress(), datagramPacket.getPort());
					DatagramPacket sendDatagramPacket;
					if (authenticated) {
						DataPacket sendDataPacket = new DataPacket(new byte[8]);
						sendDataPacket.addByte(type); // 0, requestType
						sendDataPacket.addByte(identifier); // 1, Request identifier
						sendDataPacket.addByte((byte) 1); // 2, Response, 1 = authenticated
						sendDataPacket.addShort(Globals.snapshotTick); // 3-4, Request identifier
						Entity clientEntity = clientManager.getClient(uuid);
						ClientComponent client = (ClientComponent) entityManager.getComponentInEntity(clientEntity, ClientComponent.class);
						Entity playerEntity = client.getPlayer();
						PlayerComponent player = (PlayerComponent) entityManager.getComponentInEntity(playerEntity, PlayerComponent.class);
						sendDataPacket.addShort(player.getPlayerID()); // 5-6, Request identifier
						sendDataPacket.addByte((byte) 20); // 7, End data packet
						sendDatagramPacket = new DatagramPacket(sendDataPacket.getData(), sendDataPacket.size(), datagramPacket.getAddress(), datagramPacket.getPort());
					} else {
						DataPacket sendDataPacket = new DataPacket(new byte[4]);
						sendDataPacket.addByte(type); // 0, requestType
						sendDataPacket.addByte(identifier); // 1, Request identifier
						sendDataPacket.addByte((byte) 0); // 2, Response, 0 = not authenticated
						sendDataPacket.addByte((byte) 20); // 3, End data packet
						sendDatagramPacket = new DatagramPacket(sendDataPacket.getData(), sendDataPacket.size(), datagramPacket.getAddress(), datagramPacket.getPort());
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
					Vector3f angularVelocity = new Vector3f(intX / 1000.0f, intY / 1000.0f, intZ / 1000.0f);
					int intDt = dataPacket.getInteger();
					float dt = intDt / 10000.0f;
					Entity player = clientComponent.getPlayer();
					controlShip(player, actions, angularVelocity, dt);
				}
					break;
				case CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP: {
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
					DatagramPacket sendDatagramPacket = new DatagramPacket(sendDataPacket.getData(), sendDataPacket.size(), datagramPacket.getAddress(), datagramPacket.getPort());
					udpServer.sendData(sendDatagramPacket);
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


	
	private Vector3f tempVector = new Vector3f();
	private void controlShip(Entity player, boolean[] actions, Vector3f angularVelocity, float dt) {
		PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(player, PlayerComponent.class);
		Entity shipEntity = playerComponent.getShip();
		if (shipEntity == null)
			return;
		ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(shipEntity, ShipComponent.class);
		// Fire primary
		if (actions[6])
			shipComponent.requestFirePrimary();
		if (actions[7])
			shipComponent.requestFireSecondary();
		try {
			ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(shipEntity, ObjectComponent.class);
			// System.out.println(
			// "RECEIVE VELOCITY: " + shipComponent.getLinearThrust() + " " + objectComponent.getPosition() + " " + movementComponent.getLinearVel() +
			// " " + movementComponent.getLinearAcc());

			Vector3f linearDirection = new Vector3f();
			if (actions[0])
				linearDirection.z++;
			if (actions[1])
				linearDirection.z--;
			if (actions[2])
				linearDirection.x++;
			if (actions[3])
				linearDirection.x--;
			if (actions[4])
				linearDirection.y++;
			if (actions[5])
				linearDirection.y--;

			shipComponent.getLinearThrust().add(objectComponent.getForward().mul(dt * 2 * linearDirection.z, tempVector));
			shipComponent.getLinearThrust().add(objectComponent.getRight().mul(dt * 2 * linearDirection.x, tempVector));
			shipComponent.getLinearThrust().add(objectComponent.getUp().mul(dt * 2 * linearDirection.y, tempVector));

			objectComponent.yaw(dt * angularVelocity.y);
			objectComponent.pitch(dt * angularVelocity.x);
			objectComponent.roll(dt * angularVelocity.z);
			
//			System.out.println("UDP RE: "  + shipComponent.getLinearThrust());
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
