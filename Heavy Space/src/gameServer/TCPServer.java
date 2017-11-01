package gameServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TCPServer implements Runnable {
	final String serverIP = "192.168.1.215";
	final int serverPort = 6031;
	InetAddress serverAddress;
	ServerSocket serverSocket;
	List<TCPObject> connections = new ArrayList<TCPObject>();
	Thread thread;
	private boolean shouldClose;
	private boolean acceptNewConnections;

	private GameModel gameModel;

	public TCPServer(GameModel gameModel) {
		this.gameModel = gameModel;
		try {
			serverAddress = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		try {
			serverSocket = new ServerSocket(serverPort, 100, serverAddress);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("TCP server started. Using port: " + serverPort + ". Awaiting connection.");
		thread = new Thread(this);
		thread.start();

	}

	public void requestClose() {
		shouldClose = true;
		// Force close server socket as it may be blocking the thread
		try {
			if (!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void acceptNewConnections() {
		acceptNewConnections = true;
	}

	public void disallowNewConnections() {
		acceptNewConnections = false;
	}

	@Override
	public void run() {
		while (!shouldClose) {
			// Only go in here if we allow new connections
			if (acceptNewConnections) {
				try {
					System.out.println("Awaiting connection.");
					Socket clientSocket = serverSocket.accept();
					// Close connection if we don't accept new connections
					if (!acceptNewConnections) {
						if (!clientSocket.isClosed())
							clientSocket.close();
					} else {
						System.out.println("Client connected. " + clientSocket.getInetAddress());
						connections.add(new TCPObject(clientSocket, gameModel.addPlayer("test").getDataTransferObject()));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		cleanUp();
	}

	private void cleanUp() {
		for (TCPObject tcpObject : connections) {
			tcpObject.requestClose();
		}
		try {
			if (!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public void run2() {
//		while (true) {
//			List<TCPObject> disconnections = new ArrayList<>();
//			for (TCPObject connection : connections) {
//				while (!connection.received.isEmpty()) {
//					System.out.println(connection.received.poll());
//				}
//				if (connection.isDisconnected()) {
//					disconnections.add(connection);
//					continue;
//				}
//			}
//			for (TCPObject tcpObject : disconnections) {
//				connections.remove(tcpObject);
//				System.out.println(tcpObject.socket.getInetAddress() + " removed. Active connections: " + connections.size());
//			}
//			try {
//				thread.sleep(1);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
}
