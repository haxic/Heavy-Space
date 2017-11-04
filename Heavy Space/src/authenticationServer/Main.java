package authenticationServer;

import java.sql.SQLException;

import shared.Config;

public class Main {
	public static void main(String[] args) {
		try {
			new AuthenticationServer(new Config());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
