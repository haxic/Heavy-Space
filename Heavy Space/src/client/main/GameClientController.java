package client.main;

import client.entities.Scene;

public interface GameClientController {

	public void processInputs();

	public void update();

	public Scene getScene();

	public void close();
}
