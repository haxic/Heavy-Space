package gameServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import utilities.NetworkFunctions;


public class MainGameServer {
	public static void main(String[] args) {
		ServerConfig config = new ServerConfig();

		config.port = 6029;
		config.ipType = IPType.LAN;
		config.official = false;

		if (config.official) {
			try {
				config.authenticationServerIP = InetAddress.getByName("localhost");
				config.authenticationServerPort = 5431;
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
		new GameServer(config);
	}
}