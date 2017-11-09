package client.main;

public interface ClientController {

	public void processInputs(float deltaTime);

	public void update(float deltaTime);

	public Scene getScene();

	public void close();
}
