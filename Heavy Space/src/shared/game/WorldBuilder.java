package shared.game;

import gameServer.GameModel;

public class WorldBuilder {

	public static void createTestWorld(GameModel gameModel) {
		GameEntity asteroid = new GameEntity();
		asteroid.position.set(25, 25, 25);
		asteroid.orientation.set(25, 25, 25);
		gameModel.addGameEntity(asteroid);
	}

}
