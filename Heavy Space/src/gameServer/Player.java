package gameServer;

public class Player {
	private String username;
	private DataTransferObject dto;

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
