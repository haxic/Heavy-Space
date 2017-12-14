package gameServer.core;

import java.net.InetAddress;

import shared.functionality.network.IPType;

public class ServerConfig {
	public InetAddress authenticationServerIP;
	public int authenticationServerPort;
	public InetAddress masterServerIP;
	public int masterServerPort;

	public InetAddress ip;
	public int port;
	public IPType ipType;
	public boolean official;
}
