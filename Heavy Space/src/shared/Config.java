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
		dbEndPoint = "jdbc:postgresql://ec2-23-21-92-251.compute-1.amazonaws.com/d4jfrp7pjrtdjh";
		dbUsername = "fbqkxcdwyqdbcj";
		dbPassword = "6d89f6eea619b383f076c82d1da8bfd0d784ef381648b0021ceb63467ca0b1ad";
		useSSL = true;
		gameServerDefaultPort = 6029;
		gameClientDefaultPort = 6028;
	}
}
