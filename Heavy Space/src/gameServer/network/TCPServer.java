package gameServer.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import gameServer.core.ServerConfig;
import shared.Config;

public class TCPServer implements Runnable {
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

	public void startServer(InetAddress ip, int port) throws IOException {
		InetSocketAddress serverAddress = new InetSocketAddress(ip, port);
		serverSocket = new ServerSocket();
		System.out.println(serverAddress);
		serverSocket.bind(serverAddress);
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
