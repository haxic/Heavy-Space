package tests.game.local;

import gameServer.network.IGameServerRequestHandler;

public class GameServerRequestHandlerTester implements IGameServerRequestHandler {

	@Override
	public boolean validateClient(String username, String token) {
		return username != null && token != null;
	}

}
