package client.network;

import java.net.InetAddress;

import gameServer.IPType;

public class GameServerData {
	private InetAddress ip;
	private int port;
	private IPType ipType;
	private boolean offical;

	public GameServerData(InetAddress ip, int port, IPType ipType, boolean offical) {
		this.ip = ip;
		this.port = port;
		this.ipType = ipType;
		this.offical = offical;
	}

	public InetAddress getIP() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public IPType getIPType() {
		return ipType;
	}

	public boolean isOfficial() {
		return offical;
	}
}
