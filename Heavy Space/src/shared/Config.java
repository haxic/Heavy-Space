package shared;

public class Config {
	public String authenticationServerIP;
	public int authenticationServerPort;
	public String masterServerIP;
	public int masterServerPort;
	public String dbEndPoint;
	public String dbUsername;
	public String dbPassword;
	public boolean useSSL;
	public int gameServerDefaultPort;
	public int gameClientDefaultPort;

	public final int GAME_CLIENT_PORT = 6028;
	
	public Config() {
		authenticationServerIP = "127.0.0.1";
		authenticationServerPort = 6031;
		masterServerIP = "127.0.0.1";
		masterServerPort = 6030;
		dbEndPoint = "";
		dbUsername = "";
		dbPassword = "";
		useSSL = true;
		gameServerDefaultPort = 6029;
		gameClientDefaultPort = 6028;
	}
}
