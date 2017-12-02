package client.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import client.network.GameServerData;
import gameServer.IPType;
import utilities.NetworkFunctions;

public class Main {
	public static void main(String[] args) {
		InetAddress serverIP = null;
		int serverPort = 6029;

		IPType serverIPType = IPType.LAN;

		try {
			serverIP = InetAddress.getByName("192.168.1.215");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("Could not retrieve hosting ip!");
			System.exit(0);
		}
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
