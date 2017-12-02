package shared.functionality.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TCPSocketHandler {
	private Queue<byte[]> received = new ConcurrentLinkedQueue();
	private Queue<byte[]> toSend = new ConcurrentLinkedQueue();
	private OutputHandler outputHandler;
	private InputHandler inputHandler;
	private boolean shouldClose;
	private TCPSocket tcpSocket;

	public TCPSocketHandler(TCPSocket tcpSocket) {
		this.tcpSocket = tcpSocket;
		outputHandler = new OutputHandler();
		inputHandler = new InputHandler();
	}

	public void start() {
		outputHandler.start();
		inputHandler.start();
	}
	
	// Send several data packets
	public void sendMultipleData(List<byte[]> datas) {
		for (byte[] data : datas)
			toSend.add(data);
	}

	// Send one data packet
	public void sendData(byte[] data) {
		toSend.add(data);
	}

	// Get received data
	public byte[] getData() {
		return received.poll();
	}

	public boolean isDisconnected() {
		return shouldClose;
	}

	public void disconnect() {
		shouldClose = true;
		if (!tcpSocket.isClosed()) {
			try {
				tcpSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class InputHandler extends Thread implements Runnable {
		@Override
		public void run() {
			while (!shouldClose && !tcpSocket.isClosed()) {
				try {
					byte[] data = tcpSocket.readData();
					if (data != null) {
						received.add(data);
					}
				} catch (IOException e) {
					e.printStackTrace();
					shouldClose = true;
					break;
				}
			}
		}
	}

	class OutputHandler extends Thread implements Runnable {
		@Override
		public void run() {
			while (!shouldClose) {
				while (!toSend.isEmpty() && !shouldClose && !tcpSocket.isClosed()) {
					byte[] data = toSend.poll();
					if (data == null || data.length == 0)
						continue;
					try {
						tcpSocket.sendData(data);
					} catch (IOException e) {
						e.printStackTrace();
						shouldClose = true;
						break;
					}
				}
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					shouldClose = true;
					break;
				}
			}
		}
	}

}
