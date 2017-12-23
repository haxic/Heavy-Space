package shared.functionality.network;

public enum RequestType {
	CLIENT_REQUEST_AUTHENTICATE_UDP,
	CLIENT_REQUEST_GAME_ACTION_CONTROL_SHIP,
	CLIENT_REQUEST_GAME_ACTION_SPAWN_SHIP,
	CLIENT_REQUEST_READY,
	CLIENT_REQUEST_PING,
	SERVER_REPONSE_SNAPSHOT;
	
	public byte asByte() {
		return (byte) this.ordinal();
	}
}
