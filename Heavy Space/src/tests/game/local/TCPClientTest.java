package tests.game.local;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class TCPClientTest {
	public static void main(String args[]) throws IOException {

		JFrame frame = new JFrame("FrameDemo");
		JTextArea textArea = new JTextArea(30, 80);
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.LIGHT_GRAY);
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(textArea);
		frame.pack();
		frame.setVisible(true);

		Socket clientSocket = null;
		try {
//			String serverIP = "5.186.147.73";
			 String serverIP = "192.168.1.215";
			// String serverIP = "localhost";
			int serverPort = 6031;
			textArea.append("Connecting to server: " + serverIP + ":" + serverPort + "...\n");
			clientSocket = new Socket(serverIP, serverPort);
			textArea.append("Connection established!\n");
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

			byte[] receiveData = null;
			int length;
			while ((length = in.readInt()) > 0) {
				receiveData = new byte[length];
				in.readFully(receiveData, 0, receiveData.length);
			}
			textArea.append("Server ip: " + serverIP + "\n");

			textArea.append("Received from server: " + receiveData[0] + "\n");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		clientSocket.close();
	}

	public static void asd() {
		byte[] sendData = new byte[504];
		int value = Integer.parseInt("4");
		System.out.println(value);
		byte[] inInt = intToByteArray(value);
		System.out.println(inInt.length);
		for (int i = 0; i < 3; i++)
			sendData[i] = inInt[i];

		int test = ByteBuffer.wrap(new byte[] { sendData[0], sendData[1], sendData[2], sendData[3] }).getInt();
	}

	public static byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}
}
