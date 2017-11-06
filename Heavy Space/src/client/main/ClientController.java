package client.main;

public interface ClientController {

	public void processInputs();

	public void update(float deltaTime);

	public Scene getScene();

	public void close();
}
