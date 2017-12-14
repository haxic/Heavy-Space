package client.main;

import java.net.InetAddress;
import java.net.UnknownHostException;

import client.controllers.MainController;
import client.network.GameServerData;
import gameServer.core.ServerConfig;
import utilities.FileUtils;

public class MainGameClient {
	public static void main(String[] args) {
		ServerConfig serverConfig = new ServerConfig();
		serverConfig.authenticationServerPort = 6031;
		String configString = FileUtils.loadAsString("config/config.txt");
		System.out.println(configString);
		String[] splitResult = configString.split("\\s+");
		InetAddress serverIP = null;
		try {
			serverConfig.authenticationServerIP = InetAddress.getByName(splitResult[0]);
			serverIP = InetAddress.getByName(splitResult[1]);
		} catch (UnknownHostException e) {
			System.exit(0);
		}
		int serverPort = Integer.parseInt(splitResult[2]);
		String username = splitResult[3];
		String password = splitResult[4];
		
		new MainController(new GameServerData(serverIP, serverPort, null, false), serverConfig, username, password);
	}

}
