package tests.game.online;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import gameServer.AgentManager;
import gameServer.network.IServerCommunicator;
import gameServer.network.ServerCommunicator;
import gameServer.network.TCPServer;
import gameServer.network.ValidationService;
import shared.Config;
import tests.LocalConfig;
import tests.dbsetup.DBTestSetup;
import tests.dbsetup.OnlineUserData;

public class ValidationServiceOnlineTest extends DBTestSetup {

	private static final int NUMBER_OF_THREADS = 6;
	private static final int CONNECTIONS_PER_THREAD = 5;
	IServerCommunicator serverCommunicator;
	Config config = new LocalConfig();

	@Test
	public void testValidationService() {
		LocalConfig localConfig = new LocalConfig();
		serverCommunicator = new ServerCommunicator(localConfig.authenticationServerIP + ":" + localConfig.authenticationServerPort);
		serverCommunicator.createAccount(OnlineUserData.USERNAME, OnlineUserData.PASSWORD);
		if (!serverCommunicator.authenticate(OnlineUserData.USERNAME, OnlineUserData.PASSWORD))
			fail();

		AgentManager agentmanager = new AgentManager(null);
		ValidationService validationService = new ValidationService(serverCommunicator, null, 500, false);
		TCPServer tcpServer = new TCPServer("localhost", config.gameServerDefaultPort, validationService);
		try {
			tcpServer.startServer();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		while (!tcpServer.isAccepting()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Tester[] testers = new Tester[NUMBER_OF_THREADS];
		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			testers[i] = new Tester(i, CONNECTIONS_PER_THREAD, i % 6);
		}
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
		assertEquals(30, total);
		assertEquals(10, accepted);
		assertEquals(20, failed);
		for (int i = 1; i < types.length; i++) {
			assertEquals(types[i], 5);
		}
	}

	public class Tester implements Runnable {
		private int spawnerID;
		private Thread thread;
		public int accepted;
		public int failed;
		public boolean done;
		public int[] types = new int[6];
		private boolean randomTypes = true;
		private int type;
		private int numberOfConnections;
		public int startedCounter;
		private int finishedCounter;

		public Tester(int spawnerID, int numberOfConnections) {
			this.spawnerID = spawnerID;
			this.numberOfConnections = numberOfConnections;
			thread = new Thread(this);
			thread.start();
		}

		public Tester(int spawnerID, int numberOfConnections, int type) {
			this.spawnerID = spawnerID;
			this.numberOfConnections = numberOfConnections;
			this.type = type;
			randomTypes = false;
			thread = new Thread(this);
			thread.start();
		}

		public void done() {
			finishedCounter++;
			if (startedCounter == finishedCounter && startedCounter == numberOfConnections)
				done = true;
		}

		@SuppressWarnings("resource")
		@Override
		public void run() {
			for (int i = 0; i < numberOfConnections; i++) {
				int type;
				if (randomTypes)
					type = (int) (Math.random() * 6);
				else
					type = this.type;
				types[type]++;
				new SocketTester(i, type);
				startedCounter++;
			}
		}

		class SocketTester implements Runnable {
			private Thread thread;
			private int type;
			private int id;
			private String username;
			private String token;

			public SocketTester(int id, int type) {
				this.id = id;
				this.type = type;
				username = "client-" + spawnerID + "#" + id;
				thread = new Thread(this);
				thread.start();
			}

			@SuppressWarnings("resource")
			@Override
			public void run() {
				{
					serverCommunicator.createAccount(username, username);
					String result = null;
					try {
						result = ((ServerCommunicator) serverCommunicator).getAuthenticationServerRMI().authenticate(username, username);
					} catch (RemoteException e) {
						e.printStackTrace();
						fail();
					}
					String[] splitResult = result.split("\\s+");
					token = splitResult[1];
					assertEquals(username, splitResult[2]);
				}
				try {
					Socket clientSocket = new Socket("localhost", config.gameServerDefaultPort);
					if (type == 3) {
						clientSocket.close();
						failed++;
						done();
						return;
					}
					if (type == 1) {
						thread.sleep(1000);
						failed++;
						done();
						return;
					}
					DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream in = new DataInputStream(clientSocket.getInputStream());
					String message;
					if (type == 2)
						message = "asdasdasd32u234";
					else
						message = username + " " + token;
					byte[] invalidMessage = message.getBytes();
					out.writeInt(invalidMessage.length);
					out.write(invalidMessage);
					if (type == 4) {
						clientSocket.close();
						failed++;
						done();
						return;
					}
					int length;
					byte[] data = null;
					if ((length = in.readInt()) > 0) {
						data = new byte[length];
						in.readFully(data, 0, data.length);
					}
					String result = new String(data);
					String[] splitResult = result.split(":+");
					if ("Accepted".equals(splitResult[0]))
						accepted++;
					else
						failed++;
					if (type == 5)
						clientSocket.close();
					done();
					return;
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					fail();
				}
				fail();
			}
		}
	}
}
