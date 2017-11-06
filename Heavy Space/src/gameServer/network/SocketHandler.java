package gameServer.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class SocketHandler {

	private DataInputStream in;
	private DataOutputStream out;
	private Socket socket;

	public SocketHandler(Socket socket) {
		this.socket = socket;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] readData() throws IOException {
		int length;
		byte[] data = null;
		if ((length = in.readInt()) > 0) {
			data = new byte[length];
			in.readFully(data, 0, data.length);
		}
		return data;
	}

	public void sendData(byte[] invalidMessage) throws IOException {
		out.writeInt(invalidMessage.length);
		out.write(invalidMessage);
	}

	public boolean isClosed() {
		return socket.isClosed(); // || !socket.isConnected() ||
									// socket.isInputShutdown() ||
									// socket.isOutputShutdown();
	}

	public void close() throws IOException {
		if (!socket.isClosed())
			socket.close();
	}

	public void setSoTimeout(int timeout) throws SocketException {
		socket.setSoTimeout(timeout);
	}

	public InetAddress getAddress() {
		return socket.getInetAddress();
	}

	public void requestClose() {
		try {
			close();
		} catch (IOException e) {
		}
	}

}
