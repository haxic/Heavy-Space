package gameServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shared.DataPacket;
import shared.game.GameEntity;

public class GameModel {
	Map<String, Player> players = new HashMap<String, Player>();
	List<GameEntity> gameEntities = new ArrayList<GameEntity>();

	public void addGameEntity(GameEntity asteroid) {
		gameEntities.add(asteroid);
	}

	public Player addPlayer(String username) {
		Player player = players.get(username);
		if (player != null)
			player.reconnect();
		else
			player = players.put(username, new Player(username));
		return player;
	}

	public List<DataPacket> getWorldAsData() {
		List<DataPacket> dataPackets = new ArrayList<DataPacket>();
		DataPacket dataPacket = new DataPacket(null);
		byte messageType = 10; // 0
		byte bulkSize = (byte) gameEntities.size(); // 1

		// Add message type
		dataPacket.addByte(messageType);
		// Add bulk
		dataPacket.addByte(bulkSize);
		for (GameEntity gameEntity : gameEntities) {
			// Add position
			dataPacket.addInteger((int) (gameEntity.position.x * 1000)); // 2
			dataPacket.addInteger((int) (gameEntity.position.y * 1000)); // 6
			dataPacket.addInteger((int) (gameEntity.position.z * 1000)); // 10
			// Add orientation
			dataPacket.addShort((short) (gameEntity.orientation.x * 100)); // 14
			dataPacket.addShort((short) (gameEntity.orientation.y * 100)); // 18
			dataPacket.addShort((short) (gameEntity.orientation.z * 100));
		}
		return dataPackets;
	}
}
