package client.network;

import java.net.DatagramPacket;

import shared.functionality.network.RequestType;

public class UDPRequest {

	private RequestType requestType;
	private byte identifier;
	private DatagramPacket datagramPacket;
	private boolean resend;
	private float duration;
	private float elapsed;

	public UDPRequest(RequestType requestType, byte identifier, DatagramPacket datagramPacket, boolean resend, float duration) {
		this.requestType = requestType;
		this.identifier = identifier;
		this.datagramPacket = datagramPacket;
		this.resend = resend;
		this.duration = duration;
	}

	public DatagramPacket getDatagramPacket() {
		return datagramPacket;
	}
	
	public RequestType getRequestType() {
		return requestType;
	}

	public byte getIdentifier() {
		return identifier;
	}

	public void renew(float time) {
		duration += time;
	}

	public boolean matches(RequestType requestType, byte identifier) {
		return requestType.equals(this.requestType) && identifier == this.identifier;
	}

	public void update(float dt) {
		elapsed += dt;
	}

	public boolean overDue() {
		return elapsed > duration;
	}

	public float getAccumulatedDT() {
		return elapsed;
	}

	public boolean shouldResend() {
		return resend;
	}
}
