package tests;

import org.junit.Test;

import authenticationServer.main.AuthenticationRequestHandler;
import shared.rmi.MasterServerRequestHandler;
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
		String token = arh.authenticate(username, password, ip);
		msrh.hostGameServer(token, username);
	}

}
