package gameServer.network;

public interface IServerCommunicator {

	boolean authenticate(String username, String password);

	boolean validateClient(String clientToken, String clientUsername);

}
