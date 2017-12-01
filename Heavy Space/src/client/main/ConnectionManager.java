package client.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import gameServer.Pinger;
import gameServer.components.ClientPendingComponent;
import gameServer.components.ClientValidatedComponent;
import shared.Config;
import shared.DataPacket;
import shared.functionality.ByteIdentifier;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.functionality.Globals;
import shared.functionality.UDPRequest;
import shared.functionality.UDPServer;
import shared.functionality.RequestType;
import shared.functionality.TCPSocket;
import shared.functionality.TCPSocketHandler;
import tests.LocalConfig;

public class ConnectionManager {

	private String udpIP;
	private int udpPort;
	private UDPServer udpServer;
	private TCPSocket tcpSocket;

	private String username;
	private String token;

	private Config config;
	private EventHandler eventHandler;

	private ConnectionStatus connectionStatus;
	private ConnectionStatus lastStatus;

	private List<UDPRequest> udpRequests;
	private List<UDPRequest> udpRequestsRemoved;

	private List<TCPRequest> tcpRequests;
	private List<TCPRequest> tcpRequestsRemoved;

	Pinger udpPinger;
	Pinger tcpPinger;

	private TCPSocketHandler tcpSocketHandler;
	private ByteIdentifier tcpIdentifier;
	private ByteIdentifier udpIdentifier;
	private InetAddress udpIPAddress;
	private String uuid;

	public ConnectionManager(EventHandler eventHandler, String udpIP, int udpPort, Config config) {
		this.eventHandler = eventHandler;
		this.udpIP = udpIP;
		this.udpPort = udpPort;
		this.config = config;
		tcpIdentifier = new ByteIdentifier();
		udpIdentifier = new ByteIdentifier();
		udpPinger = new Pinger();
		tcpPinger = new Pinger();
		udpServer = new UDPServer(udpIP, udpPort);
		username = "testclient";
		token = "whatevertoken";
		connectionStatus = ConnectionStatus.Disconnected;
		udpRequests = new ArrayList<UDPRequest>();
		tcpRequests = new ArrayList<TCPRequest>();
		udpRequestsRemoved = new ArrayList<UDPRequest>();
		tcpRequestsRemoved = new ArrayList<TCPRequest>();
	}

	boolean joinServer(String serverIP, int serverPort) {
		try {
			udpServer.startServer();
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		boolean validated = true;
		try {
			connectionStatus = ConnectionStatus.Connecting;
			tcpSocket = new TCPSocket(new Socket(serverIP, serverPort));
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
		dataPacket.addByte(identifier); // 1, Packet identifier
		dataPacket.addString(uuid);
		dataPacket.addByte((byte) 20); // 1, End data packet

		try {
			udpIPAddress = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			disconnect();
			return false;
		}

		DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.getCurrentDataSize(), udpIPAddress, config.gameServerDefaultPort);
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
				System.out.println("CLIENT UDP RECEIVED: " + RequestType.CLIENT_REQUEST_AUTHENTICATE_UDP + " " + identifier);
				UDPRequest request = findMatchingUDPRequest(requestType, identifier);
				if (request == null)
					break;
				udpRequests.remove(request);
				short tick = dataPacket.getShort(); // 2-3, Tick
				udpPinger.handlePing(request.getTimestamp(), tick);
				byte response = dataPacket.getByte(); // 4, Request identifier
				if (response == 1) {
					System.out.println("ACCEPTED, SENDING TCP PACKET: " + RequestType.CLIENT_REQUEST_READY);
					eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_CONNECT, "Connected to server."));
					DataPacket sendDataPacket = new DataPacket(new byte[3]);
					sendDataPacket.addByte((byte) RequestType.CLIENT_REQUEST_READY.ordinal());
					sendDataPacket.addByte(tcpIdentifier.get());
					sendDataPacket.addByte((byte) 20);
					tcpSocketHandler.sendData(sendDataPacket.getData());
					break;
				}
				System.out.println("FAILED " + RequestType.CLIENT_REQUEST_READY);
				disconnect();
				eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_FAILED_TO_CONNECT, "Failed to authenticate UDP."));
			}
				break;
			case SERVER_REPONSE_SPAWN_ENTITIES: {
				System.out.println("CLIENT UDP RECEIVED: " + RequestType.SERVER_REPONSE_SPAWN_ENTITIES);
				short tick = dataPacket.getShort(); // 1-2, Tick
				int packetNumber = dataPacket.getByte(); // 3, Packet number
				int numberOfEntities = dataPacket.getByte(); // 4, Number of entities
				for (int i = 0; i < numberOfEntities; i++) {
					int eeid = dataPacket.getInteger(); // 5, Entity id
					int entityType = dataPacket.getByte(); // 9, Entity type (obstacle, ship, projectile etc)
					int entityVariation = dataPacket.getByte(); // 10, Entity variation (what variation of the type)
					float positionX = dataPacket.getInteger() / 1000.0f; // 11-14, Position x
					float positionY = dataPacket.getInteger() / 1000.0f; // 15-18, Position y
					float positionZ = dataPacket.getInteger() / 1000.0f; // 19-22, Position z
					Vector3f position = new Vector3f(positionX, positionY, positionZ);
					eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_CREATE_UNIT, tick, eeid, entityType, entityVariation, position));
				}
			}
				break;
			case SERVER_REPONSE_UPDATE_ENTITIES: {
				short tick = dataPacket.getShort(); // 1, Tick
				int packetNumber = dataPacket.getByte(); // 3, Packet number
				int numberOfEntities = dataPacket.getByte(); // 4, Number of entities
				for (int i = 0; i < numberOfEntities; i++) {
					int eeid = dataPacket.getInteger(); // 5-8, Entity id
					float positionX = dataPacket.getInteger() / 1000.0f; // 9-12, Position x
					float positionY = dataPacket.getInteger() / 1000.0f; // 13-16, Position y
					float positionZ = dataPacket.getInteger() / 1000.0f; // 17-20, Position z
					byte end = dataPacket.getByte();
					Vector3f position = new Vector3f(positionX, positionY, positionZ);
					// System.out.println("SERVER_REPONSE_UPDATE_ENTITIES" + position.x + " " + position.y + " " + position.z + " " + end);
					eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_UPDATE_UNIT, tick, eeid, position));
				}
			}
				break;
			case CLIENT_REQUEST_PING: {
				byte identifier = dataPacket.getByte(); // 1, Request identifier
//				System.out.println("CLIENT UDP RECEIVED: " + RequestType.CLIENT_REQUEST_PING + " " + identifier);
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
//				System.out.println("CLIENT TCP RECEIVED: " + RequestType.CLIENT_REQUEST_PING + " " + identifier);
				TCPRequest request = findMatchingTCPRequest(requestType, identifier);
				if (request == null)
					break;
				tcpRequests.remove(request);
				short tick = dataPacket.getShort(); // 3-4, Tick
				tcpPinger.handlePing(request.getTimestamp(), tick);
			}
				break;
			case CLIENT_REQUEST_READY: {
				System.out.println("CLIENT TCP RECEIVED: " + RequestType.CLIENT_REQUEST_READY + " " + identifier);
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
		System.out.println("TCP/UDP ping: " + tcpPinger.toString() + " " + udpPinger.toString());
		pingTCP();
		pingUDP();
	}

	private void pingTCP() {
		if (tcpSocketHandler == null)
			return;
		RequestType requestType = RequestType.CLIENT_REQUEST_PING;
		byte identifier = tcpIdentifier.get();
		DataPacket dataPacket = new DataPacket(new byte[3]);
		dataPacket.addByte(requestType.asByte());
		dataPacket.addByte(identifier);
		dataPacket.addByte((byte) 20);
		tcpRequests.add(new TCPRequest(requestType, identifier));
		tcpSocketHandler.sendData(dataPacket.getData());
	}

	private void pingUDP() {
		if (udpServer == null || uuid == null)
			return;
		RequestType requestType = RequestType.CLIENT_REQUEST_PING;
		byte identifier = udpIdentifier.get();
		DataPacket dataPacket = new DataPacket(new byte[71]);
		dataPacket.addByte(requestType.asByte());
		dataPacket.addByte(identifier);
		dataPacket.addString(uuid);
		dataPacket.addInteger(udpPinger.getAverageMS());
		dataPacket.addByte((byte) 20);
		DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.getCurrentDataSize(), udpIPAddress, config.gameServerDefaultPort);
		udpRequests.add(new UDPRequest(requestType, identifier, datagramPacket, false));
		udpServer.sendData(datagramPacket);
	}

}
