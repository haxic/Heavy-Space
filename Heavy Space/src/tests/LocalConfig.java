package tests;

import shared.Config;

public class LocalConfig extends Config {
	
	public LocalConfig() {
		authenticationServerIP = "127.0.0.1";
		authenticationServerPort = 5431;
		masterServerIP = "127.0.0.1";
		masterServerPort = 5430;
		dbEndPoint = "jdbc:postgresql://127.0.0.1:5432/testdb";
		dbUsername = "haxic";
		dbPassword = "";
		useSSL = false;
	}
}
