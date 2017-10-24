package shared.dbo;

import java.sql.Date;

public class GameServer {
	public static final String GAME_SERVER = "game_server";
	public static final String ACCOUNT_ID = "account_id";
	public static final String SERVER_IP = AuthenticationToken.CLIENT_IP;
	public static final String LAST_CHECKED = "last_checked";
	private int accountID;
	private String serverIP;
	private Date lastChecked;

	public GameServer(int accountID, String serverIP, Date lastChecked) {
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
	
	public Date getLastChecked() {
		return lastChecked;
	}

	@Override
	public String toString() {
		return "[" + accountID + ", " + serverIP + ", " + lastChecked + "]";
	}
}
