package client.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import gameServer.network.SocketHandler;
import gameServer.network.UDPServer;
import shared.Config;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import tests.LocalConfig;

public class ConnectionManager {

	String udpIP;
	int udpPort;
	UDPServer udp;
	SocketHandler tcp;

	String username;
	String token;

	private Config config;
	private EventHandler eventHandler;

	public ConnectionManager(EventHandler eventHandler, String udpIP, int udpPort, Config config) {
		this.eventHandler = eventHandler;
		this.udpIP = udpIP;
		this.udpPort = udpPort;
		this.config = config;
		udp = new UDPServer(udpIP, udpPort);
		username = "testclient";
		token = "whatevertoken";
	}

	boolean joinServer(String serverIP, int serverPort) {
		try {
			udp.startServer();
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
			return false;
		}

		boolean validated = false;
		try {
			tcp = new SocketHandler(new Socket(serverIP, serverPort));
			tcp.sendData((username + " " + token).getBytes());

			byte[] data = tcp.readData();
			if (data == null) {
				closeConnection();
				eventHandler.addEvent(new Event(EventType.JOIN_SERVER_FAILED, "Unable to read data"));
				return false;
			}
			String result = new String(data);
			String[] splitResult;
			try {
				splitResult = result.split(":+");
				if ("Accepted".equals(splitResult[0]))
					validated = true;
			} catch (Exception e) {
				closeConnection();
				eventHandler.addEvent(new Event(EventType.JOIN_SERVER_FAILED, e.getMessage()));
				return false;
			}
		} catch (IOException e) {
			closeConnection();
			eventHandler.addEvent(new Event(EventType.JOIN_SERVER_FAILED, e.getMessage()));
			return false;
		}
		if (!validated) {
			closeConnection();
			eventHandler.addEvent(new Event(EventType.JOIN_SERVER_FAILED, "Not validated"));
			return false;
		}
		return true;
		// TODO: Maybe send UDP package for server to identify client UDP
		// ip/port
	}

	private void closeConnection() {
		if (udp != null)
			udp.requestClose();
		if (tcp != null)
			tcp.requestClose();
	}

	public void disconnect() {
		closeConnection();
	}
}
