package tests.servers.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import authenticationServer.AuthenticationRequestHandler;
import masterServer.MasterServerRequestHandler;
import shared.Config;
import shared.dbo.GameServerInfo;
import tests.dbsetup.DBTestSetup;

public class BasicServerTests extends DBTestSetup {

	public BasicServerTests(String endpoint, String username, String password) {
		super(endpoint, username, password);
	}

	@Test
	public void testAllServerRequests() {
		AuthenticationRequestHandler arh = new AuthenticationRequestHandler(this);
		MasterServerRequestHandler msrh = new MasterServerRequestHandler(this);
		String username = "test";
		String password = "test1234";
		String ip = "ip";
		
		// Create account
		arh.createAccount(username, password, ip);
		
		// Authenticate (and get authentication token)
		String result = arh.authenticate(username, password, ip);
		System.out.println(result);
		String[] splitResult = result.split("\\s+");
		assertEquals(splitResult[0], Config.MASTER_SERVER_IP + ":" + Config.MASTER_SERVER_PORT);
		String token = splitResult[1];
		
		// Host game
		msrh.hostGameServer(token, username);
		LocalDateTime first = null;
		LocalDateTime second = null;
		try {
			first = getGameServerDAO().getGameServer(1).getLastChecked();
		} catch (SQLException e) {
			fail();
		}
		
		// Re-host game
		msrh.hostGameServer(token, username);
		try {
			second = getGameServerDAO().getGameServer(1).getLastChecked();
		} catch (SQLException e) {
			fail();
		}
		assertTrue(first.isBefore(second));
		
		// Get server list from master server
		List<GameServerInfo> serverList = msrh.getGameServerList(token, username);
		assertTrue(!serverList.isEmpty());
		
		// The client tells the master server that it wants to join the server
		String serverIP = msrh.joinGameServer(token, username, serverList.get(0).getServerIP());
		assertEquals(ip, serverList.get(0).getServerIP());
		assertEquals(serverIP, serverList.get(0).getServerIP());
		
		// The game server asks the master server if the client is authenticated
		boolean clientCheck = msrh.checkClient(token, username, token, username);
		assertTrue(clientCheck);
		
		// The game server heart-beats
		String newToken = msrh.heartbeat(token, username);

		// Check client with new token
		clientCheck = msrh.checkClient(newToken, username, newToken, username);
		assertTrue(clientCheck);
	}

}
