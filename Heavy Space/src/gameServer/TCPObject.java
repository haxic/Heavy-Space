package gameServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPObject {
	Socket socket;
	DataOutputStream out;
	DataInputStream in;
	InputHandler inputHandler;
	OutputHandler outputHandler;
	private boolean disconnected;
	boolean shouldClose;
	DataTransferObject dto;

	public TCPObject(Socket socket, DataTransferObject player) throws IOException {
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
					length = in.readInt();
					if (length > 0) {
						byte[] data = new byte[length];
						in.readFully(data, 0, data.length);
						dto.receiveData(data);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				if (!socket.isClosed())
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			disconnected = true;
		}
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
						out.writeInt(message.length);
						out.write(message);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				if (!socket.isClosed())
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			disconnected = true;
		}
	}

	public boolean isDisconnected() {
		return disconnected;
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
