package tests;

import java.sql.SQLException;

import masterServer.MasterServer;
import shared.Config;

public class MasterServerTestMain {

	public static void main(String[] args) {
		try {
			new MasterServer(new LocalConfig());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
