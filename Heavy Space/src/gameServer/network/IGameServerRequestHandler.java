package gameServer.network;

public interface IGameServerRequestHandler {

	boolean validateClient(String username, String token);

}
