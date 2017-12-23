package client.controllers;

import client.gameData.Scene;

public interface IController {

	public void processInputs();

	public void update(float dt);

	public Scene getScene();

	public void close();

	public int getTick();

}
