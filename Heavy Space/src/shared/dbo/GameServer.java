package shared.dbo;

import java.time.LocalDateTime;

public class GameServer {
	public static final String GAME_SERVER = "game_server";
	public static final String ACCOUNT_ID = "account_id";
	public static final String SERVER_IP = AuthenticationToken.CLIENT_IP;
	public static final String LAST_CHECKED = "last_checked";
	private int accountID;
	private String serverIP;
	private LocalDateTime lastChecked;

	public GameServer(int accountID, String serverIP, LocalDateTime lastChecked) {
		super();
		this.accountID = accountID;
		this.serverIP = serverIP;
		this.lastChecked = lastChecked;
	}

	public int getID() {
		return accountID;
	}

	public String getServerIP() {
		return serverIP;
	}
	
	public LocalDateTime getLastChecked() {
		return lastChecked;
	}

	@Override
	public String toString() {
		return "[" + accountID + ", " + serverIP + ", " + lastChecked + "]";
	}
}