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

import org.joml.Vector3f;

import client.inputs.ShipControls;
import shared.functionality.ByteIdentifier;
import shared.functionality.DataPacket;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.functionality.Globals;
import shared.functionality.Pinger;
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

	private Pinger udpPinger;
	private Pinger tcpPinger;

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
		username = "testclient";
		token = "whatevertoken";
		connectionStatus = ConnectionStatus.Disconnected;
		udpRequests = new ArrayList<UDPRequest>();
		tcpRequests = new ArrayList<TCPRequest>();
		udpRequestsRemoved = new ArrayList<UDPRequest>();
		tcpRequestsRemoved = new ArrayList<TCPRequest>();
	}

	public boolean joinServer(GameServerData gameServerData) {
		this.gameServerData = gameServerData;
		InetAddress clientIP;

		if (gameServerData.isOfficial() && token == null) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, "Tried to join an official server without being authenticated."));
			return false;
		}

		try {
			clientIP = NetworkFunctions.getIP(gameServerData.getIPType().asHost());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try {
			udpServer.startServer(clientIP, 6028);
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

		udpRequests.add(new UDPRequest(RequestType.CLIENT_REQUEST_AUTHENTICATE_UDP, identifier, datagramPacket, true));
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

	public void handleUDPRequests() {
		if (udpServer == null)
			return;
		DatagramPacket datagramPacket;
		while ((datagramPacket = udpServer.getData()) != null) {
			DataPacket dataPacket = new DataPacket(datagramPacket.getData());
			byte type = dataPacket.getByte(); // 0, Request type
			RequestType requestType = RequestType.values()[type & 0xFF];
			switch (requestType) {
			case CLIENT_REQUEST_AUTHENTICATE_UDP: {
				byte identifier = dataPacket.getByte(); // 1, Request identifier
				UDPRequest request = findMatchingUDPRequest(requestType, identifier);
				if (request == null)
					break;
				udpRequests.remove(request);
				byte response = dataPacket.getByte(); // 2, Request identifier
				if (response == 1) {
					short tick = dataPacket.getShort(); // 3-4, Tick
					udpPinger.handlePing(request.getTimestamp(), tick);
					short playerID = dataPacket.getShort(); // 5-6, Player id
					this.playerID = playerID;
					eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_CONNECT, "Connected to server."));
					DataPacket sendDataPacket = new DataPacket(new byte[3]);
					sendDataPacket.addByte((byte) RequestType.CLIENT_REQUEST_READY.ordinal());
					sendDataPacket.addByte(tcpIdentifier.get());
					sendDataPacket.addByte((byte) 20);
					tcpSocketHandler.sendData(sendDataPacket.getData());
					break;
				}
				disconnect();
				eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, "Failed to authenticate UDP."));
			}
				break;
			case SERVER_REPONSE_SPAWN_ENTITIES: {
				short tick = dataPacket.getShort(); // 1-2, Tick
				int packetNumber = dataPacket.getByte(); // 3, Packet number
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
					Vector3f forward = null;
					Vector3f up = null;
					Vector3f right = null;
					Vector3f velocity = null;
					if (entityType == 1) {
						float velocityX = dataPacket.getFloat(); // 25-28, Velocity x
						float velocityY = dataPacket.getFloat(); // 29-32, Velocity y
						float velocityZ = dataPacket.getFloat(); // 33-36, Velocity z
						velocity = new Vector3f(velocityX, velocityY, velocityZ);
					}
					if (entityType != 1) {
						float forwardX = dataPacket.getFloat(); // 22-25, Forward x
						float forwardY = dataPacket.getFloat(); // 26-29, Forward y
						float forwardZ = dataPacket.getFloat(); // 30-33, Forward z
						forward = new Vector3f(forwardX, forwardY, forwardZ);
						float upX = dataPacket.getFloat(); // 34-29, Up x
						float upY = dataPacket.getFloat(); // 38-41, Up y
						float upZ = dataPacket.getFloat(); // 42-45, Up z
						up = new Vector3f(upX, upY, upZ);
						float rightX = dataPacket.getFloat(); // 46-49, Right x
						float rightY = dataPacket.getFloat(); // 50-53, Right y
						float rightZ = dataPacket.getFloat(); // 54-57, Right z
						right = new Vector3f(rightX, rightY, rightZ);
						System.out.println(right);
					}
					eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_CREATE_UNIT, tick, eeid, entityType, entityVariation, playerID, position, forward, up, right, velocity));
				}
			}
				break;
			case SERVER_REPONSE_UPDATE_ENTITIES: {
				short tick = dataPacket.getShort(); // 1-2, Tick
				int packetNumber = dataPacket.getByte(); // 3, Packet number
				int numberOfEntities = dataPacket.getByte(); // 4, Number of entities
				for (int i = 0; i < numberOfEntities; i++) {
					int eeid = dataPacket.getInteger(); // 5-8, Entity id
					byte flagsByte = dataPacket.getByte(); // 9, Flags

					boolean[] flags = BitConverter.booleanArrayFromByte(flagsByte);
					Vector3f position = null;
					Vector3f forward = null;
					Vector3f up = null;
					Vector3f right = null;
					int killingEeid = 0;

					if (flags[0]) {
						float positionX = dataPacket.getFloat(); // 10-13, Position x
						float positionY = dataPacket.getFloat(); // 14-17, Position y
						float positionZ = dataPacket.getFloat(); // 18-21, Position z
						position = new Vector3f(positionX, positionY, positionZ);
						float forwardX = dataPacket.getFloat(); // 22-25, Forward x
						float forwardY = dataPacket.getFloat(); // 26-29, Forward y
						float forwardZ = dataPacket.getFloat(); // 30-33, Forward z
						forward = new Vector3f(forwardX, forwardY, forwardZ);
						float upX = dataPacket.getFloat(); // 34-37, Up x
						float upY = dataPacket.getFloat(); // 38-41, Up y
						float upZ = dataPacket.getFloat(); // 42-45, Up z
						up = new Vector3f(upX, upY, upZ);
						float rightX = dataPacket.getFloat(); // 46-49, Right x
						float rightY = dataPacket.getFloat(); // 50-53, Right y
						float rightZ = dataPacket.getFloat(); // 54-57, Right z
						right = new Vector3f(rightX, rightY, rightZ);
					}
					if (flags[2]) {
						killingEeid = dataPacket.getInteger(); // 22-25, Killing entity id
					}
					eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_UPDATE_UNIT, tick, eeid, flags, position, forward, up, right, killingEeid));
				}
				eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_UPDATE_SNAPSHOT, tick));
			}
				break;
			case SERVER_REPONSE_UPDATE: {
				short tick = dataPacket.getShort(); // 1-2, Tick
				eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_UPDATE_SNAPSHOT, tick));
			}
				break;
			case CLIENT_REQUEST_PING: {
				byte identifier = dataPacket.getByte(); // 1, Request identifier
				UDPRequest request = findMatchingUDPRequest(requestType, identifier);
				if (request == null)
					break;
				short tick = dataPacket.getShort(); // 2-3, Request identifier
				udpRequests.remove(request);
				udpPinger.handlePing(request.getTimestamp(), tick);
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
			long elapsed = Globals.now - request.getRenewedTimestamp();
			if (elapsed > 10000 || udpIdentifier.check() + 1 == request.getIdentifier()) {
				udpRequestsRemoved.add(request);
			} else if (elapsed > 1000 && request.shouldResend()) {
				request.renew();
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

	public void handleTCPRequests() {
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
				tcpPinger.handlePing(request.getTimestamp(), tick);
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
			long elapsed = Globals.now - request.getRenewedTimestamp();
			if (elapsed > 10000 || udpIdentifier.check() + 1 == request.getIdentifier()) {
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
		udpRequests.add(new UDPRequest(requestType, identifier, datagramPacket, false));
		udpServer.sendData(datagramPacket);
	}

	public void sendShipActions(ShipControls shipControls) {
		RequestType requestType = RequestType.CLIENT_REQUEST_GAME_ACTION_CONTROL_SHIP;
		byte identifier = udpIdentifier.get();

		DataPacket dataPacket = new DataPacket(new byte[85]);
		dataPacket.addByte(requestType.asByte()); // 0
		dataPacket.addString(uuid); // 1-64
		dataPacket.addByte(identifier); // 65
		dataPacket.addShort(Globals.tick); // 66-67
		dataPacket.addByte(BitConverter.byteFromBooleanArray(new boolean[] { shipControls.forwardThrust, shipControls.reverseThrust, shipControls.starboardThrust, shipControls.portThrust,
				shipControls.ascend, shipControls.decend, shipControls.primary, shipControls.secondary })); // 68
		dataPacket.addInteger((int) (shipControls.angularDirection.x * 1000)); // 69-72, Rotation x
		dataPacket.addInteger((int) (shipControls.angularDirection.y * 1000)); // 73-76, Rotation y
		dataPacket.addInteger((int) (shipControls.angularDirection.z * 1000)); // 77-80, Rotation z
		dataPacket.addInteger((int) (Globals.dt * 10000)); // 81-84, Rotation z
		DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), gameServerData.getIP(), gameServerData.getPort());
		udpServer.sendData(datagramPacket);
	}

	public void requestSpawnShip(Vector3f position) {
		RequestType requestType = RequestType.CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP;
		byte identifier = udpIdentifier.get();

		DataPacket dataPacket = new DataPacket(new byte[69]);
		dataPacket.addByte(requestType.asByte()); // 0
		dataPacket.addString(uuid); // 1-64
		dataPacket.addByte(identifier); // 65
		dataPacket.addShort(Globals.tick); // 66-67
		dataPacket.addByte((byte) 20); // 68
		DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), gameServerData.getIP(), gameServerData.getPort());
		udpServer.sendData(datagramPacket);
	}

	public short getPlayerID() {
		return playerID;
	}
}
