package shared.rmi;

public class GameServerInfo {
	String serverName = "unknown";
	String serverIP;
	int currentPlayers = -1;
	int maxPlayers = -1;
	int ping = -1;

	public GameServerInfo(String serverIP) {
		this.serverIP = serverIP;
	}

	@Override
	public String toString() {
		return "[" + serverName + ", " + serverIP + ", " + currentPlayers + "/" + maxPlayers + " " + ping + "]";
	}
}
