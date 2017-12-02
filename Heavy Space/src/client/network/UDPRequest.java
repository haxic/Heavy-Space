package client.network;

import java.net.DatagramPacket;

import shared.functionality.Globals;
import shared.functionality.network.RequestType;

public class UDPRequest {

	private long timestamp;
	private long renewedTimestamp;
	private RequestType requestType;
	private byte identifier;
	private DatagramPacket datagramPacket;
	private boolean resend;

	public UDPRequest(RequestType requestType, byte identifier, DatagramPacket datagramPacket, boolean resend) {
		timestamp = Globals.now;
		renewedTimestamp = timestamp;
		this.requestType = requestType;
		this.identifier = identifier;
		this.datagramPacket = datagramPacket;
		this.resend = resend;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getRenewedTimestamp() {
		return renewedTimestamp;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public byte getIdentifier() {
		return identifier;
	}

	public DatagramPacket getDatagramPacket() {
		return datagramPacket;
	}
	
	public boolean shouldResend() {
		return resend;
	}

	public void renew() {
		renewedTimestamp = Globals.now;
	}

	public boolean matches(RequestType requestType, byte identifier) {
		return requestType.equals(this.requestType) && identifier == this.identifier;
	}
}
