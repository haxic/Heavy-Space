package tests.game.local;

import org.junit.Test;

import gameServer.GameModel;
import gameServer.Player;
import gameServer.DataTransferObject;
import shared.game.WorldBuilder;

public class TestConnectionFlow {

	@Test
	public void testConnectionFlow() {
		// create server
		GameModel gameModel = new GameModel();
		// init world
		WorldBuilder.createTestWorld(gameModel);
		// init server
		// player connects with username and token
		Player player = gameModel.addPlayer("tester");
		// server validates player
		// server send world to player
		player.sendData(gameModel.getWorldAsData());
		// player sends fire command to server
	}
}
