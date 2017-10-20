package shared;

import java.util.List;

public class GameServerInfo {
	int accountID;
	String name;
	String ip;
	// TODO: Remove this when and if transitioning into large scale (mmo) servers
	List<BasicPlayerStats> players;
	int maxPlayers;
	

}
