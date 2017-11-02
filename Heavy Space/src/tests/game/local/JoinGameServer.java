package tests.game.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import gameServer.network.TCPServer;
import gameServer.network.ValidationService;

public class JoinGameServer {

	private static final int NUMBER_OF_THREADS = 50;
	private static final int CONNECTIONS_PER_THREAD = 50;

	@Test
	public void testConnectionFlow() {
		GameServerRequestHandlerTester gsrhTester = new GameServerRequestHandlerTester();
		ValidationService validationService = new ValidationService(gsrhTester);
		TCPServer tcpServer = new TCPServer(validationService);
		tcpServer.startServer();

		while (!tcpServer.isAccepting()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Tester[] testers = new Tester[NUMBER_OF_THREADS];
		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			testers[i] = new Tester(i);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int accepted = 0;
		int failed = 0;
		for (int i = 0; i < testers.length; i++) {
			accepted += testers[i].accepted;
			failed += testers[i].failed;
		}
		System.out.println((accepted + failed) + " " + accepted + " " + failed);
	}

	class Tester implements Runnable {
		int id;
		Thread thread;
		public int accepted, failed;

		public Tester(int id) {
			this.id = id;
			thread = new Thread(this);
			thread.start();
		}

		@SuppressWarnings("resource")
		@Override
		public void run() {
			for (int i = 0; i < CONNECTIONS_PER_THREAD; i++) {
				String client = "client-" + id + "#" + i;
				try {
					Socket clientSocket = new Socket(TCPServer.SERVER_IP, TCPServer.SERVER_PORT);
					DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream in = new DataInputStream(clientSocket.getInputStream());
					String message = client + " " + client + "TOKEN";
					byte[] invalidMessage = message.getBytes();
					out.writeInt(invalidMessage.length);
					out.write(invalidMessage);

					int length;
					byte[] data = null;
					if ((length = in.readInt()) > 0) {
						data = new byte[length];
						in.readFully(data, 0, data.length);
					}
					String result = new String(data);
					System.out.println(result);
					String[] splitResult = result.split(":+");
					if ("Accepted".equals(splitResult[0]))
						accepted++;
					else
						failed++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
