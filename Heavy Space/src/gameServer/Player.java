package gameServer;

import org.joml.Vector3f;

import gameServer.network.DataTransferObject;

public class Player {
	private String username;
	private DataTransferObject dto;
	public Vector3f position = new Vector3f();

	public Player(String username) {
		this.username = username;
		dto = new DataTransferObject();
	}

	public String getUsername() {
		return username;
	}

	public DataTransferObject getDataTransferObject() {
		return dto;
	}

	public void reconnect() {
		dto = new DataTransferObject();
	}

}
