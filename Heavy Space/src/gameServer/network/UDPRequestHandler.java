package gameServer.network;

import java.net.DatagramPacket;

import org.joml.Vector3f;

import gameServer.components.ClientComponent;
import gameServer.components.PlayerComponent;
import gameServer.components.ShipComponent;
import gameServer.core.ClientManager;
import gameServer.events.PlayerActionEvent;
import gameServer.events.PlayerSpawnEvent;
import hecs.Entity;
import hecs.EntityManager;
import hevent.EventManager;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.functionality.DataPacket;
import shared.functionality.network.RequestType;
import shared.functionality.network.UDPServer;
import utilities.BitConverter;

public class UDPRequestHandler {

	private EntityManager entityManager;
	private EventManager eventManager;
	private ClientManager clientManager;
	private UDPServer udpServer;

	public UDPRequestHandler(EntityManager entityManager, EventManager eventManager, ClientManager clientManager, UDPServer udpServer) {
		this.entityManager = entityManager;
		this.eventManager = eventManager;
		this.clientManager = clientManager;
		this.udpServer = udpServer;
	}

	public void process(float dt, short sstick) {
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
						sendDataPacket.addShort(sstick); // 3-4, Request identifier
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
					float angularVelocityX = dataPacket.getFloat();
					float angularVelocityY = dataPacket.getFloat();
					float angularVelocityZ = dataPacket.getFloat();
					Vector3f angularVelocity = new Vector3f(angularVelocityX, angularVelocityY, angularVelocityZ);
					float angularVelocityDT = dataPacket.getFloat();
					Entity player = clientComponent.getPlayer();
					eventManager.createEvent(new PlayerActionEvent(player, actions, angularVelocity, angularVelocityDT, dt));
				}
					break;
				case CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP: {
					System.out.println("SPAWN SHIP FFS");
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
					
					eventManager.createEvent(new PlayerSpawnEvent(player));
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
					sendDataPacket.addShort(sstick); // 2-3
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



}
