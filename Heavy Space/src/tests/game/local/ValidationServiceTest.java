package tests.game.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.mindrot.jbcrypt.BCrypt;

import gameServer.ClientManager;
import gameServer.PlayerManager;
import gameServer.network.IServerCommunicator;
import gameServer.network.TCPServer;
import gameServer.network.ValidationService;
import shared.Config;
import tests.LocalConfig;
import tests.dbsetup.OnlineUserData;
import tests.implementations.ServerCommunicatorLocal;
import tests.implementations.TestAgentManager;
import tests.implementations.TestPlayerManager;

public class ValidationServiceTest {

	private static final int NUMBER_OF_THREADS = 18;
	private static final int CONNECTIONS_PER_THREAD = 20;
	private String ip = "localhost";
	Config config = new LocalConfig();

	@Test
	public void testValidationService() {
		IServerCommunicator serverCommunicator = new ServerCommunicatorLocal();
		if (!serverCommunicator.authenticate(OnlineUserData.USERNAME, OnlineUserData.PASSWORD))
			fail();
		PlayerManager playerManager = new TestPlayerManager();
		ClientManager agentManager = new TestAgentManager(playerManager);
		ValidationService validationService = new ValidationService(serverCommunicator, agentManager, 500, false);
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

		assertEquals(total, 360);
		assertEquals(accepted, 120);
		assertEquals(failed, 240);
		for (int i = 1; i < types.length; i++) {
			assertEquals(types[i], 60);
		}
	}

	public class Tester implements Runnable {
		private int id;
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

		public Tester(int id, int numberOfConnections) {
			this.id = id;
			this.numberOfConnections = numberOfConnections;
			thread = new Thread(this);
			thread.start();
		}

		public Tester(int id, int numberOfConnections, int type) {
			this.id = id;
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
				username = "client-" + id + "#" + id;
				token = BCrypt.hashpw(username, BCrypt.gensalt());
				thread = new Thread(this);
				thread.start();
			}

			@SuppressWarnings("resource")
			@Override
			public void run() {
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
					fail();
				}
				fail();
			}
		}
	}
}
