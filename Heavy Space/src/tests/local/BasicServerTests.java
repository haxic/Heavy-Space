package tests.local;

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

	@Test
	public void createAccountAndAuthenticate() {
		AuthenticationRequestHandler arh = new AuthenticationRequestHandler(this);
		MasterServerRequestHandler msrh = new MasterServerRequestHandler(this);
		String username = "test";
		String password = "test1234";
		String ip = "ip";
		arh.createAccount(username, password, ip);
		String result = arh.authenticate(username, password, ip);
		String[] splitResult = result.split("\\s+");
		assertEquals(splitResult[0], Config.MASTER_SERVER_IP + ":" + Config.MASTER_SERVER_PORT);
	}

	@Test
	public void hostGameServerAndJoin() {
		AuthenticationRequestHandler arh = new AuthenticationRequestHandler(this);
		MasterServerRequestHandler msrh = new MasterServerRequestHandler(this);
		String username = "test";
		String password = "test1234";
		String ip = "ip";
		arh.createAccount(username, password, ip);
		String result = arh.authenticate(username, password, ip);
		String[] splitResult = result.split("\\s+");
		String token = splitResult[1];
		msrh.hostGameServer(token, username);
		List<GameServerInfo> serverList = msrh.getGameServerList(token, username);
		assertTrue(!serverList.isEmpty());
		String serverIP = msrh.joinGameServer(token, username, serverList.get(0).getServerIP());
		assertEquals(ip, serverList.get(0).getServerIP());
		assertEquals(serverIP, serverList.get(0).getServerIP());
	}

	@Test
	public void rehosthostGameServer() {
		AuthenticationRequestHandler arh = new AuthenticationRequestHandler(this);
		MasterServerRequestHandler msrh = new MasterServerRequestHandler(this);
		String username = "test";
		String password = "test1234";
		String ip = "ip";
		arh.createAccount(username, password, ip);
		String result = arh.authenticate(username, password, ip);
		String[] splitResult = result.split("\\s+");
		String token = splitResult[1];
		msrh.hostGameServer(token, username);
		LocalDateTime first = null;
		LocalDateTime second = null;
		try {
			first = getGameServerDAO().getGameServer(1).getLastChecked();
		} catch (SQLException e) {
			fail();
		}
		msrh.hostGameServer(token, username);
		try {
			second = getGameServerDAO().getGameServer(1).getLastChecked();
		} catch (SQLException e) {
			fail();
		}
		System.out.println(first + " " + second);
		assertTrue(first.isBefore(second));
	}

}
