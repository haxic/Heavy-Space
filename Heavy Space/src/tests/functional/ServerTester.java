package tests.functional;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import shared.functionality.network.UDPServer;
import tests.LocalConfig;

public class ServerTester {

	public ServerTester() throws InterruptedException, UnknownHostException, SocketException {
		LocalConfig config = new LocalConfig();
//		TCPSocket socketHandler;
//		try {
//			socketHandler = new TCPSocket(new Socket("localhost", config.gameServerDefaultPort));
//			socketHandler.sendData("hello world".getBytes());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		UDPServer udpServer = new UDPServer("localhost", 6028);
		udpServer.startServer();
		InetAddress address = InetAddress.getByName("localhost");
		byte[] data1 = "TEST1111".getBytes();
		DatagramPacket datagramPacket1 = new DatagramPacket(data1, data1.length, address, config.gameServerDefaultPort);
		byte[] data2 = "asdsadsdasda".getBytes();
		DatagramPacket datagramPacket2 = new DatagramPacket(data2, data2.length, address, config.gameServerDefaultPort);
		byte[] data3 = "ghhhhhhhhhhh".getBytes();
		DatagramPacket datagramPacket3 = new DatagramPacket(data3, data3.length, address, config.gameServerDefaultPort);
		byte[] data4 = "eeeeeee".getBytes();
		DatagramPacket datagramPacket4 = new DatagramPacket(data4, data4.length, address, config.gameServerDefaultPort);
		byte[] data5 = "cccccccc".getBytes();
		DatagramPacket datagramPacket5 = new DatagramPacket(data5, data5.length, address, config.gameServerDefaultPort);
		Thread.sleep(1000);
		udpServer.sendData(datagramPacket1);
		Thread.sleep(1000);
		udpServer.sendData(datagramPacket2);
		Thread.sleep(1000);
		udpServer.sendData(datagramPacket3);
		Thread.sleep(1000);
		udpServer.sendData(datagramPacket4);
		Thread.sleep(1000);
		udpServer.sendData(datagramPacket5);
	}

	public static void main(String[] args) {
		try {
			new ServerTester();
		} catch (InterruptedException | UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
	}
}
