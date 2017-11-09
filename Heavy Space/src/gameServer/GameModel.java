package gameServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hecs.Entity;
import shared.DataPacket;

public class GameModel {
	Map<String, Player> players = new HashMap<String, Player>();
	List<Entity> gameEntities = new ArrayList<Entity>();

	public void addGameEntity(Entity asteroid) {
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
		for (Entity gameEntity : gameEntities) {
			// Add position
			dataPacket.addInteger((int) (gameEntity.getPosition().x * 1000)); // 2
			dataPacket.addInteger((int) (gameEntity.getPosition().y * 1000)); // 6
			dataPacket.addInteger((int) (gameEntity.getPosition().z * 1000)); // 10
			// Add orientation
			dataPacket.addShort((short) (gameEntity.getRotation().x * 100)); // 14
			dataPacket.addShort((short) (gameEntity.getRotation().y * 100)); // 18
			dataPacket.addShort((short) (gameEntity.getRotation().z * 100));
		}
		return dataPackets;
	}
}
