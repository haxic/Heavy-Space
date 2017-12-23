package client.network;

import shared.functionality.network.RequestType;

public class TCPRequest {
	private RequestType requestType;
	private byte identifier;
	private float accumulatedDT;
	private float lifeTime;

	public TCPRequest(RequestType requestType, byte identifier) {
		this.requestType = requestType;
		this.identifier = identifier;
		lifeTime = 1f;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public byte getIdentifier() {
		return identifier;
	}

	public void renew(float time) {
		lifeTime += 1;
	}

	public boolean matches(RequestType requestType, byte identifier) {
		return requestType.equals(this.requestType) && identifier == this.identifier;
	}

	public void update(float dt) {
		accumulatedDT += dt;
	}

	public boolean overDue() {
		return accumulatedDT > lifeTime;
	}

	public float getAccumulatedDT() {
		return accumulatedDT;
	}
}
