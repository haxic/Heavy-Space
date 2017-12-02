package client.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import client.network.GameServerData;
import gameServer.IPType;
import utilities.NetworkFunctions;

public class MainGameClient {
	public static void main(String[] args) {
		InetAddress serverIP = null;
		int serverPort = 6029;

		IPType serverIPType = IPType.LAN;

		try {
			serverIP = InetAddress.getByName("5.186.147.73");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("Could not retrieve hosting ip!");
			System.exit(0);
		}
		serverIP = null;
		if (serverIP == null) {
			try {
				serverIP = NetworkFunctions.getIP(serverIPType);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Could not retrieve hosting ip!");
				System.exit(0);
			}
		}
		new GameClient(new GameServerData(serverIP, serverPort, serverIPType, false));
	}

}
