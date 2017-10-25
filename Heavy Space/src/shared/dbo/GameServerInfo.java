package shared.dbo;

public class GameServerInfo {

	String serverName = "unknown";
	String serverIP;
	int currentPlayers = -1;
	int maxPlayers = -1;
	int ping = -1;

	public GameServerInfo(String serverIP) {
		this.serverIP = serverIP;
	}

	public String getServerName() {
		return serverName;
	}

	public String getServerIP() {
		return serverIP;
	}

	public int getCurrentPlayers() {
		return currentPlayers;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getPing() {
		return ping;
	}

	@Override
	public String toString() {
		return "[" + serverName + ", " + serverIP + ", " + currentPlayers + "/" + maxPlayers + " " + ping + "]";
	}

}
