package gameServer.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TCPServer implements Runnable {
	public static final String SERVER_IP = "192.168.1.215";
	public static final int SERVER_PORT = 6031;
	InetAddress serverAddress;
	ServerSocket serverSocket;
	Thread thread;
	private boolean shouldClose;
	private boolean acceptNewConnections;
	private ValidationService validationService;
	private boolean isBlocking;

	public TCPServer(ValidationService validationService) {
		this.validationService = validationService;
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

	public void startServer() {
		try {
			serverAddress = InetAddress.getByName(SERVER_IP);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		try {
			serverSocket = new ServerSocket(SERVER_PORT, 100, serverAddress);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("TCP server started. Using port: " + SERVER_PORT + ".");
		acceptNewConnections();
		thread = new Thread(this);
		thread.start();
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
					isBlocking = true;
					Socket clientSocket = serverSocket.accept();
					isBlocking = false;
					validationService.handleNewConnection(clientSocket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isAccepting() {
		return isBlocking;
	}
}
