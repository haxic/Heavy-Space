package client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import client.inputs.ShipControls;
import shared.functionality.ByteIdentifier;
import shared.functionality.DataPacket;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.functionality.Pinger;
import shared.functionality.SnapshotSequenceType;
import shared.functionality.network.RequestType;
import shared.functionality.network.TCPSocket;
import shared.functionality.network.TCPSocketHandler;
import shared.functionality.network.UDPServer;
import utilities.BitConverter;
import utilities.NetworkFunctions;

public class ConnectionManager {

	private UDPServer udpServer;
	private TCPSocket tcpSocket;

	private String username;
	private String token;

	private EventHandler eventHandler;

	private ConnectionStatus connectionStatus;
	private ConnectionStatus lastStatus;

	private List<UDPRequest> udpRequests;
	private List<UDPRequest> udpRequestsRemoved;

	private List<TCPRequest> tcpRequests;
	private List<TCPRequest> tcpRequestsRemoved;

	public Pinger udpPinger;
	public Pinger tcpPinger;

	private TCPSocketHandler tcpSocketHandler;
	private ByteIdentifier tcpIdentifier;
	private ByteIdentifier udpIdentifier;
	private String uuid;
	private GameServerData gameServerData;
	private short playerID;

	public ConnectionManager(EventHandler eventHandler) {
		this.eventHandler = eventHandler;

		tcpIdentifier = new ByteIdentifier();
		udpIdentifier = new ByteIdentifier();
		udpPinger = new Pinger();
		tcpPinger = new Pinger();
		udpServer = new UDPServer();
		connectionStatus = ConnectionStatus.Disconnected;
		udpRequests = new ArrayList<UDPRequest>();
		tcpRequests = new ArrayList<TCPRequest>();
		udpRequestsRemoved = new ArrayList<UDPRequest>();
		tcpRequestsRemoved = new ArrayList<TCPRequest>();
	}

	public void setUser(String username, String token) {
		this.username = username;
		this.token = token;
	}

	public boolean joinServer(GameServerData gameServerData) {
		this.gameServerData = gameServerData;

		if (gameServerData.isOfficial() && token == null) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, "Tried to join an official server without being authenticated."));
			return false;
		}

		try {
			udpServer.startServer(InetAddress.getLocalHost(), 6028);
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		boolean validated = true;
		try {
			connectionStatus = ConnectionStatus.Connecting;
			tcpSocket = new TCPSocket(new Socket(gameServerData.getIP(), gameServerData.getPort()));
			tcpSocket.sendData((username + " " + token).getBytes());

			byte[] data = tcpSocket.readData();
			if (data == null) {
				disconnect();
				eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, "Unable to read data"));
				return false;
			}
			String result = new String(data);
			String[] splitResult;
			try {
				splitResult = result.split(":+");
				if (!"Accepted".equals(splitResult[0]))
					validated = false;
				uuid = splitResult[1];
				if (uuid == null)
					validated = false;
			} catch (Exception e) {
				disconnect();
				eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, e.getMessage()));
				e.printStackTrace();
				return false;
			}
		} catch (IOException e) {
			disconnect();
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, e.getMessage()));
			e.printStackTrace();
			return false;
		}
		if (!validated) {
			disconnect();
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, "Not validated"));
			return false;
		}
		tcpSocketHandler = new TCPSocketHandler(tcpSocket);
		tcpSocketHandler.start();
		// TODO: Maybe send UDP package for server to identify client UDP
		// ip/port
		byte requestType = (byte) RequestType.CLIENT_REQUEST_AUTHENTICATE_UDP.ordinal();
		byte identifier = udpIdentifier.get();
		DataPacket dataPacket = new DataPacket(new byte[67]);
		dataPacket.addByte(requestType); // 0, requestType
		dataPacket.addString(uuid); // 1-64
		dataPacket.addByte(identifier); // 65, Packet identifier
		dataPacket.addByte((byte) 20); // 66, End data packet
		DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), gameServerData.getIP(), gameServerData.getPort());

		udpRequests.add(new UDPRequest(RequestType.CLIENT_REQUEST_AUTHENTICATE_UDP, identifier, datagramPacket, true, 2));
		udpServer.sendData(datagramPacket);
		connectionStatus = ConnectionStatus.Authenticating;
		pingUDP();
		pingUDP();
		pingUDP();
		return true;
	}

	public void disconnect() {
		if (udpServer != null)
			udpServer.requestClose();
		if (tcpSocket != null)
			tcpSocket.requestClose();
		connectionStatus = ConnectionStatus.Disconnected;
		udpRequests.clear();
	}

	public void handleUDPRequests(float dt) {
		if (udpServer == null)
			return;
		DatagramPacket datagramPacket;
		while ((datagramPacket = udpServer.getData()) != null) {
			DataPacket dataPacket = new DataPacket(datagramPacket.getData());
			byte requestTypeByte = dataPacket.getByte(); // 0, Request type
			RequestType requestType = RequestType.values()[requestTypeByte & 0xFF];
			switch (requestType) {
			case CLIENT_REQUEST_AUTHENTICATE_UDP: {
				byte identifier = dataPacket.getByte(); // 1, Request identifier
				UDPRequest request = findMatchingUDPRequest(requestType, identifier);
				System.out.println("CLIENT_REQUEST_AUTHENTICATE_UDP");
				if (request == null)
					break;
				System.out.println("HAS REQUEST");
				udpRequests.remove(request);
				byte response = dataPacket.getByte(); // 2, Request identifier
				if (response == 1) {
					System.out.println("ACCEPTED BY SERVER");
					short tick = dataPacket.getShort(); // 3-4, Tick
					udpPinger.handlePing(request.getAccumulatedDT(), tick);
					short playerID = dataPacket.getShort(); // 5-6, Player id
					this.playerID = playerID;
					eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_CONNECT, "Connected to server."));
					DataPacket sendDataPacket = new DataPacket(new byte[3]);
					sendDataPacket.addByte((byte) RequestType.CLIENT_REQUEST_READY.ordinal());
					sendDataPacket.addByte(tcpIdentifier.get());
					sendDataPacket.addByte((byte) 20);
					tcpSocketHandler.sendData(sendDataPacket.getData());
					System.out.println("SEND READY");
					break;
				}
				disconnect();
				eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, "Failed to authenticate UDP."));
			}
				break;
			case SERVER_REPONSE_SNAPSHOT: {
				short snapshotTick = dataPacket.getShort(); // 1-2, Tick
				byte packetNumber = dataPacket.getByte(); // 3
				boolean hasNext = false;
				do {
					byte snapshotSequenceTypeByte = dataPacket.getByte(); // 4
					SnapshotSequenceType snapshotSequenceType = SnapshotSequenceType.values()[snapshotSequenceTypeByte & 0xFF];
					switch (snapshotSequenceType) {
					case CREATE: {
						int numberOfEntities = dataPacket.getByte(); // 4, Number of entities
						for (int i = 0; i < numberOfEntities; i++) {
							int eeid = dataPacket.getInteger(); // 5, Entity id
							int entityType = dataPacket.getByte(); // 9, Entity type (obstacle, ship, projectile etc)
							int entityVariation = dataPacket.getByte(); // 10, Entity variation (what variation of the type)
							short playerID = 0;
							if (entityType != 2)
								playerID = dataPacket.getShort(); // 11-12, Entity variation (what variation of the type)
							float positionX = dataPacket.getFloat(); // 13-16, Position x
							float positionY = dataPacket.getFloat(); // 17-20, Position y
							float positionZ = dataPacket.getFloat(); // 21-24, Position z
							Vector3f position = new Vector3f(positionX, positionY, positionZ);
							Quaternionf orientation = null;
							Vector3f velocity = null;
							if (entityType == 1) {
								float velocityX = dataPacket.getFloat(); // 25-28, Velocity x
								float velocityY = dataPacket.getFloat(); // 29-32, Velocity y
								float velocityZ = dataPacket.getFloat(); // 33-36, Velocity z
								velocity = new Vector3f(velocityX, velocityY, velocityZ);
							}
							if (entityType != 1) {
								float orientationX = dataPacket.getFloat(); // 22-25, Forward x
								float orientationY = dataPacket.getFloat(); // 26-29, Forward y
								float orientationZ = dataPacket.getFloat(); // 30-33, Forward z
								float orientationW = dataPacket.getFloat(); // 34-37, orientation z
								orientation = new Quaternionf(orientationX, orientationY, orientationZ, orientationW);
							}
							eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SNAPSHOT_CREATE, snapshotTick, eeid, entityType, entityVariation, playerID, position, orientation, velocity));
						}
					}
						hasNext = true;
						break;
					case UPDATE: {
						int numberOfEntities = dataPacket.getByte(); // 4, Number of entities
						for (int i = 0; i < numberOfEntities; i++) {
							int eeid = dataPacket.getInteger(); // 5-8, Entity id
							byte flagsByte = dataPacket.getByte(); // 9, Flags

							boolean[] flags = BitConverter.booleanArrayFromByte(flagsByte);
							Vector3f position = null;
							Quaternionf orientation = null;
							int killingEeid = 0;

							if (flags[0]) {
								float positionX = dataPacket.getFloat(); // 10-13, Position x
								float positionY = dataPacket.getFloat(); // 14-17, Position y
								float positionZ = dataPacket.getFloat(); // 18-21, Position z
								position = new Vector3f(positionX, positionY, positionZ);
								float orientationX = dataPacket.getFloat(); // 22-25, orientation x
								float orientationY = dataPacket.getFloat(); // 26-29, orientation y
								float orientationZ = dataPacket.getFloat(); // 30-33, orientation z
								float orientationW = dataPacket.getFloat(); // 34-37, orientation z
								orientation = new Quaternionf(orientationX, orientationY, orientationZ, orientationW);
							}
							if (flags[2]) {
								killingEeid = dataPacket.getInteger(); // 22-25, Killing entity id
							}
							eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SNAPSHOT_UPDATE, snapshotTick, eeid, flags, position, orientation, killingEeid));
						}
					}
						hasNext = true;
						break;
					default:
						if (!hasNext)
							eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SNAPSHOT_END, snapshotTick));
						hasNext = false;
						break;
					}
				} while (hasNext);
			}
				break;
			case CLIENT_REQUEST_PING: {
				byte identifier = dataPacket.getByte(); // 1, Request identifier
				UDPRequest request = findMatchingUDPRequest(requestType, identifier);
				if (request == null)
					break;
				short tick = dataPacket.getShort(); // 2-3, Request identifier
				udpRequests.remove(request);
				udpPinger.handlePing(request.getAccumulatedDT(), tick);
			}
				break;
			default:
				break;
			}
		}

		if (lastStatus != connectionStatus) {
			lastStatus = connectionStatus;
		}

		for (UDPRequest request : udpRequests) {
			request.update(dt);
			if (request.overDue() || udpIdentifier.check() + 1 == request.getIdentifier()) {
				udpRequestsRemoved.add(request);
			} else if (request.overDue() && request.shouldResend()) {
				request.renew(1);
				udpServer.sendData(request.getDatagramPacket());
			}
		}
		for (UDPRequest request : udpRequestsRemoved)
			udpRequests.remove(request);
		udpRequestsRemoved.clear();
	}

	private UDPRequest findMatchingUDPRequest(RequestType requestType, byte identifier) {
		for (UDPRequest request : udpRequests)
			if (request.matches(requestType, identifier))
				return request;
		return null;
	}

	public void handleTCPRequests(float dt) {
		if (tcpSocketHandler == null)
			return;
		byte[] data;
		while ((data = tcpSocketHandler.getData()) != null) {
			DataPacket dataPacket = new DataPacket(data);
			byte type = dataPacket.getByte(); // 0, Request type
			byte identifier = dataPacket.getByte(); // 1, Request identifier, used for response
			RequestType requestType = RequestType.values()[type & 0xFF];
			switch (requestType) {
			case CLIENT_REQUEST_PING: {
				TCPRequest request = findMatchingTCPRequest(requestType, identifier);
				if (request == null)
					break;
				tcpRequests.remove(request);
				short tick = dataPacket.getShort(); // 3-4, Tick
				tcpPinger.handlePing(request.getAccumulatedDT(), tick);
			}
				break;
			case CLIENT_REQUEST_READY: {
			}
				break;
			default:
				break;
			}
		}

		for (TCPRequest request : tcpRequests) {
			request.update(dt);
			if (request.overDue() || udpIdentifier.check() + 1 == request.getIdentifier()) {
				tcpRequestsRemoved.add(request);
			}
		}
		for (TCPRequest request : tcpRequestsRemoved)
			tcpRequests.remove(request);
		tcpRequestsRemoved.clear();
	}

	private TCPRequest findMatchingTCPRequest(RequestType requestType, byte identifier) {
		for (TCPRequest request : tcpRequests)
			if (request.matches(requestType, identifier))
				return request;
		return null;
	}

	public void ping() {
		pingTCP();
		pingUDP();
	}

	private void pingTCP() {
		if (tcpSocketHandler == null)
			return;
		RequestType requestType = RequestType.CLIENT_REQUEST_PING;
		byte identifier = tcpIdentifier.get();
		DataPacket dataPacket = new DataPacket(new byte[5]);
		dataPacket.addByte(requestType.asByte()); // 0
		dataPacket.addByte(identifier); // 1
		dataPacket.addShort(udpPinger.getAverageMS()); // 2-3
		dataPacket.addByte((byte) 20); // 4
		tcpRequests.add(new TCPRequest(requestType, identifier));
		tcpSocketHandler.sendData(dataPacket.getData());
	}

	private void pingUDP() {
		if (udpServer == null || uuid == null)
			return;
		RequestType requestType = RequestType.CLIENT_REQUEST_PING;
		byte identifier = udpIdentifier.get();
		DataPacket dataPacket = new DataPacket(new byte[69]);
		dataPacket.addByte(requestType.asByte()); // 0
		dataPacket.addString(uuid); // 1-64
		dataPacket.addByte(identifier); // 65
		dataPacket.addShort(udpPinger.getAverageMS()); // 66-67
		dataPacket.addByte((byte) 20); // 68
		DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), gameServerData.getIP(), gameServerData.getPort());
		udpRequests.add(new UDPRequest(requestType, identifier, datagramPacket, false, 2));
		udpServer.sendData(datagramPacket);
	}

	public void sendShipActions(int tick, float dt, ShipControls shipControls) {
		RequestType requestType = RequestType.CLIENT_REQUEST_GAME_ACTION_CONTROL_SHIP;
		byte identifier = udpIdentifier.get();

		DataPacket dataPacket = new DataPacket(new byte[85]);
		dataPacket.addByte(requestType.asByte()); // 0
		dataPacket.addString(uuid); // 1-64
		dataPacket.addByte(identifier); // 65
		dataPacket.addShort((short) tick); // 66-67
		dataPacket.addByte(BitConverter.byteFromBooleanArray(new boolean[] { shipControls.forwardThrust, shipControls.reverseThrust, shipControls.starboardThrust, shipControls.portThrust,
				shipControls.ascend, shipControls.decend, shipControls.primary, shipControls.secondary })); // 68
		dataPacket.addFloat(shipControls.angularDirection.x); // 69-72, Rotation x
		dataPacket.addFloat(shipControls.angularDirection.y); // 73-76, Rotation y
		dataPacket.addFloat(shipControls.angularDirection.z); // 77-80, Rotation z
		dataPacket.addFloat(dt); // 81-84, Rotation z
		DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), gameServerData.getIP(), gameServerData.getPort());
		udpServer.sendData(datagramPacket);
	}

	public void requestSpawnShip(int tick, Vector3f position) {
		RequestType requestType = RequestType.CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP;
		byte identifier = udpIdentifier.get();
		DataPacket dataPacket = new DataPacket(new byte[69]);
		dataPacket.addByte(requestType.asByte()); // 0
		dataPacket.addString(uuid); // 1-64
		dataPacket.addByte(identifier); // 65
		dataPacket.addShort((short) tick); // 66-67
		dataPacket.addByte((byte) 20); // 68
		DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), gameServerData.getIP(), gameServerData.getPort());
		udpServer.sendData(datagramPacket);
	}

	public short getPlayerID() {
		return playerID;
	}
}
