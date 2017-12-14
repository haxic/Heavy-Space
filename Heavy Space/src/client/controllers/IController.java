package client.controllers;

import client.gameData.Scene;

public interface IController {

	public void processInputs();

	public void update();

	public Scene getScene();

	public void close();
}
