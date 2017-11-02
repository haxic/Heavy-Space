package gameServer.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import gameServer.GameModel;

public class TCPObject {
	Socket socket;
	DataOutputStream out;
	DataInputStream in;
	InputHandler inputHandler;
	OutputHandler outputHandler;
	boolean shouldClose;
	DataTransferObject dto;

	public TCPObject(Socket socket, GameModel gameModel) throws IOException {
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());		
		outputHandler = new OutputHandler();
		outputHandler.start();
		inputHandler = new InputHandler();
		inputHandler.start();
	}

	class InputHandler extends Thread implements Runnable {
		@Override
		public void run() {
			while (!shouldClose && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
				int length;
				try {
					while ((length = in.readInt()) > 0) {
						System.out.println("Data received!");
						byte[] data = new byte[length];
						in.readFully(data, 0, data.length);
						dto.receiveData(data);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				if (!socket.isClosed())
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			disconnect();
		}

	}

	private void disconnect() {
		dto.disconnect();
	}

	class OutputHandler extends Thread implements Runnable {
		@Override
		public void run() {
			while (!shouldClose && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
				while (dto.hasMessage()) {
					byte[] message = dto.getMessage();
					if (message == null || message.length == 0)
						continue;
					try {
						System.out.println("Data send!");
						out.writeInt(message.length);
						out.write(message);
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
			try {
				if (!socket.isClosed())
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			disconnect();
		}
	}

	public void cleanUp() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void requestClose() {
		shouldClose = true;
	}
}
