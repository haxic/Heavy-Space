package masterServer;

import java.sql.SQLException;

import shared.Config;

public class Main {

	public static void main(String[] args) {
		try {
			new MasterServer(new Config());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
