package tests;

import java.sql.SQLException;

import authenticationServer.AuthenticationServer;
import shared.Config;

public class AuthenticationServerTestMain {
	public static void main(String[] args) {
		try {
			new AuthenticationServer(new LocalConfig());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
