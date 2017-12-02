package gameServer.components;

import java.net.InetAddress;
import java.util.List;

import hecs.Entity;
import hecs.EntityComponent;
import hecs.EntityContainer;
import shared.functionality.network.TCPSocket;
import shared.functionality.network.TCPSocketHandler;

public class ClientComponent extends EntityComponent implements EntityContainer {
	private Entity player;
	private TCPSocketHandler tcpSocketHandler;
	private InetAddress udpInetAddress;
	private int udpPort;
	private String uuid;
	private String username;
	private String token;

	public ClientComponent(TCPSocket tcpSocket, String uuid, String username, String token) {
		this.tcpSocketHandler = new TCPSocketHandler(tcpSocket);
		this.uuid = uuid;
		this.username = username;
		this.token = token;
	}

	public Entity getPlayer() {
		return player;
	}

	public void start() {
		tcpSocketHandler.start();
	}

	public boolean isDisconnected() {
		return tcpSocketHandler.isDisconnected();
	}

	public InetAddress getUDPAddress() {
		return getUdpInetAddress();
	}

	public InetAddress getUdpInetAddress() {
		return udpInetAddress;
	}

	public void setUDPInetAddress(InetAddress udpInetAddress) {
		this.udpInetAddress = udpInetAddress;
	}

	public int getUDPPort() {
		return udpPort;
	}

	public void setUDPPort(int udpPort) {
		this.udpPort = udpPort;
	}
	
	public int getUdpPort() {
		return udpPort;
	}

	public String getUuid() {
		return uuid;
	}

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}

	@Override
	public void detach(Entity entity) {
	}

	public void setPlayer(Entity player) {
		this.player = player;
		player.attach(this);
	}

	@Override
	protected void removeComponent() {
		player = null;
		disconnect();
	}

	public void disconnect() {
		tcpSocketHandler.disconnect();
	}
	
	// Send several data packets
	public void sendMultipleData(List<byte[]> dataPackets) {
		tcpSocketHandler.sendMultipleData(dataPackets);
	}

	// Send one data packet
	public void sendData(byte[] dataPacket) {
		tcpSocketHandler.sendData(dataPacket);
	}

	// Get received data
	public byte[] getData() {
		return tcpSocketHandler.getData();
	}
}
