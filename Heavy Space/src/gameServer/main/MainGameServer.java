package gameServer.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import gameServer.core.GameServer;
import gameServer.core.ServerConfig;
import shared.functionality.network.IPType;
import utilities.NetworkFunctions;

public class MainGameServer {
	public static void main(String[] args) {
		ServerConfig config = new ServerConfig();
		config.port = 6029;
		config.ipType = IPType.LAN;
		config.official = true;

		if (config.official) {
			try {
				config.authenticationServerIP = InetAddress.getByName("192.168.1.215");
				config.authenticationServerPort = 6031;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("Could not retrieve authentication server ip!");
				System.exit(0);
			}
		}

		try {
			config.ip = NetworkFunctions.getIP(config.ipType);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not retrieve hosting ip!");
			System.exit(0);
		}
		String username = "testserver";
		String password = "testserver";
		new GameServer(config, username, password);
	}
}