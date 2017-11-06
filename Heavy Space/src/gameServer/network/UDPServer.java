package gameServer.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPServer {
	DatagramSocket serverSocket;
	InputHandler inputHandler;
	OutputHandler outputHandler;
	Queue<byte[]> received = new ConcurrentLinkedQueue();
	Queue<DatagramPacket> toSend = new ConcurrentLinkedQueue();
	boolean shouldClose;
	String ip;
	int port;

	public UDPServer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public void startServer() throws UnknownHostException, SocketException {
		InetAddress serverIPAddress = InetAddress.getByName(ip);
		InetSocketAddress serverAddress = new InetSocketAddress(serverIPAddress, port);
		serverSocket = new DatagramSocket(null);
		serverSocket.bind(serverAddress);
		System.out.println("UDP server started. " + serverIPAddress.getHostAddress() + ":" + port);
		outputHandler = new OutputHandler();
		outputHandler.start();
		inputHandler = new InputHandler();
		inputHandler.start();
	}

	// Send several data packets
	public void sendMultipleData(List<DatagramPacket> datagramPackets) {
		for (DatagramPacket datagramPacket : datagramPackets)
			toSend.add(datagramPacket);
	}

	// Send one data packet
	public void sendData(DatagramPacket datagramPacket) {
		toSend.add(datagramPacket);
	}

	// Get received data
	public byte[] getData() {
		return received.poll();
	}

	public void requestClose() {
		shouldClose = true;
	}

	private void disconnect() {
		if (!serverSocket.isClosed())
			serverSocket.close();
	}

	class InputHandler extends Thread implements Runnable {
		@Override
		public void run() {
			while (!shouldClose && !serverSocket.isClosed()) {
				byte[] receiveData = new byte[508];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				byte[] data = receivePacket.getData();
				if (data != null) {
					received.add(data);
				}
			}
		}
	}

	class OutputHandler extends Thread implements Runnable {
		@Override
		public void run() {
			while (!shouldClose) {
				while (!toSend.isEmpty() && !shouldClose && !serverSocket.isClosed()) {
					DatagramPacket datagramPacket = toSend.poll();
					if (datagramPacket.getData() == null || datagramPacket.getData().length == 0)
						continue;
					try {
						serverSocket.send(datagramPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			disconnect();
		}
	}

}
