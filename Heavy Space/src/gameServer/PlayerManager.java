package gameServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PlayerManager {
	public Map<String, Player> players;

	public PlayerManager() {
		players = new HashMap<>();
	}

	public Player getPlayer(String username) {
		return players.get(username);
	}

	public Player createPlayer(String username) {
		Player player = new Player(username);
		players.put(username, player);
		return player;
	}

}
