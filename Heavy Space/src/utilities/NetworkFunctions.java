package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import shared.functionality.network.IPType;

public class NetworkFunctions {

	public static InetAddress getIP(IPType ipType) throws UnknownHostException, MalformedURLException, IOException {
		switch (ipType) {
		case Localhost:
			return InetAddress.getByName("localhost");
		case LAN:
			return InetAddress.getLocalHost();
		case External:
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String result = in.readLine(); // you get the IP as a String
			in.close();
			return InetAddress.getByName(result);
		default:
			return null;
		}
	}

}
