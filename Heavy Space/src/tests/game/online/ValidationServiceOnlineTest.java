package tests.game.online;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.Naming;

import org.junit.Test;
import static org.junit.Assert.fail;
import org.mindrot.jbcrypt.BCrypt;

import gameServer.network.TCPServer;
import gameServer.network.ValidationService;
import shared.Config;
import shared.rmi.IAuthenticationServerRMI;
import tests.dbsetup.OnlineUserData;
import tests.game.local.ValidationServiceTest;

public class ValidationServiceOnlineTest {

	private static final int NUMBER_OF_THREADS = 18;
	private static final int CONNECTIONS_PER_THREAD = 20;

	@Test
	public void testValidationService() {
		// Connect to RMI
		IAuthenticationServerRMI authenticationServerRMI = (IAuthenticationServerRMI) Naming.lookup("rmi://localhost:" + Config.AUTHENTICATION_SERVER_PORT + "/authenticate");
		// Authenticate and fetch result
		String result = authenticationServerRMI.authenticate(OnlineUserData.USERNAME, OnlineUserData.PASSWORD);
		// Get master server ip and authentication token from result
		String[] splitResult = result.split("\\s+");
		String masterServerIP = splitResult[0];
		String token = splitResult[1];
		
		GameServerRequestHandlerTester gsrhTester = new GameServerRequestHandlerTester();
		ValidationService validationService = new ValidationService(gsrhTester, 500);
		TCPServer tcpServer = new TCPServer(validationService);
		tcpServer.startServer();

		while (!tcpServer.isAccepting()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ValidationServiceTest.Tester[] testers = new ValidationServiceTest.Tester[NUMBER_OF_THREADS];
		if (NUMBER_OF_THREADS > 1)
			for (int i = 0; i < NUMBER_OF_THREADS; i++) {
				testers[i] = new ValidationServiceTest.Tester(i, CONNECTIONS_PER_THREAD, i % 6);
			}
		else
			testers[0] = new ValidationServiceTest.Tester(0, CONNECTIONS_PER_THREAD, 5);
		boolean done = true;
		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			done = true;
			for (int i = 0; i < testers.length; i++) {
				if (!testers[i].done)
					done = false;
			}
		} while (!done);
		int accepted = 0;
		int failed = 0;
		int total = 0;
		int[] types = new int[6];
		for (int i = 0; i < testers.length; i++) {
			total += testers[i].startedCounter;
			accepted += testers[i].accepted;
			failed += testers[i].failed;
			for (int j = 0; j < types.length; j++) {
				types[j] += testers[i].types[j];
			}
		}
		System.out.print("Validation counter:" + validationService.getValidationCounterCurrentValue() + " Active validations:" + validationService.getNumberOfCurrentlyActiveClientValidators() + " Total: " + total + " Accepted: " + accepted + " Failed: " + failed + " Types: [");
		System.out.print(types[0]);
		for (int i = 1; i < types.length; i++) {
			System.out.print(", " + types[i]);
		}
		System.out.println("]");
	}
	

}
