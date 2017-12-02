package tests.functional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class GetIP {
	public static void main(String[] args) {
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine(); // you get the IP as a String
			in.close();
			System.out.println("External ip: " + ip);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress());

			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements();) {
				NetworkInterface e = n.nextElement();

				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements();) {
					InetAddress addr = a.nextElement();
					System.out.println("  " + addr.getHostAddress());
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		try {
			InetAddress ipAddr = InetAddress.getLocalHost();
			System.out.println(ipAddr.getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
