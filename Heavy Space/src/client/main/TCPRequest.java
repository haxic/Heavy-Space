package client.main;

import shared.functionality.RequestType;

public class TCPRequest {
	private long timestamp;
	private long renewedTimestamp;
	private RequestType requestType;
	private byte identifier;

	public TCPRequest(RequestType requestType, byte identifier) {
		timestamp = System.currentTimeMillis();
		renewedTimestamp = timestamp;
		this.requestType = requestType;
		this.identifier = identifier;
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

	public void renew() {
		renewedTimestamp = System.currentTimeMillis();
	}

	public boolean matches(RequestType requestType, byte identifier) {
		return requestType.equals(this.requestType) && identifier == this.identifier;
	}
}
