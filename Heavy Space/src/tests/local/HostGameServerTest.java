package tests.local;

import java.util.List;

import org.junit.Test;

import authenticationServer.AuthenticationRequestHandler;
import masterServer.MasterServerRequestHandler;
import shared.rmi.GameServerInfo;
import tests.dbsetup.DBTestSetup;

public class HostGameServerTest {

	@Test
	public void hostGameServer() {
		DBTestSetup testdal = new DBTestSetup();
		AuthenticationRequestHandler arh = new AuthenticationRequestHandler(testdal);
		MasterServerRequestHandler msrh = new MasterServerRequestHandler(testdal);
		String username = "test";
		String password = "test1234";
		String ip = "ip";
		arh.createAccount(username, password, ip);
		String result = arh.authenticate(username, password, ip);
		String[] splitResult = result.split("\\s+");
		String masterServerIP = splitResult[0];
		String token = splitResult[1];
		msrh.hostGameServer(token, username);
		List<GameServerInfo> serverList = msrh.getGameServerListForClient(token, username);
		System.out.println(serverList);
	}

}
