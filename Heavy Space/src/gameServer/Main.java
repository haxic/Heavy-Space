package gameServer;

import shared.Config;

public class Main {
	public static void main(String[] args) {
		Config config = new Config();
		new GameServer(config, "localhost", config.gameServerDefaultPort, true);
	}
}
