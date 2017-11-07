package client.main;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.components.ActorComponent;
import client.entities.Light;
import client.inputs.KeyboardHandler;
import hecs.Entity;
import hecs.EntityManager;
import shared.Config;
import shared.components.UnitComponent;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;

public class MenuController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private GameFactory gameFactory;
	private EntityManager entityManager;

	private Config config;

	private static final int KEY_DISCONNECT = GLFW.GLFW_KEY_ESCAPE;
	private static final int KEY_JOIN = GLFW.GLFW_KEY_SPACE;

	Entity dragon;

	public MenuController(EntityManager entityManager, EventHandler eventHandler, GameFactory gameFactory, Config config) {
		this.entityManager = entityManager;
		this.eventHandler = eventHandler;
		this.gameFactory = gameFactory;
		this.config = config;
		scene = new Scene(entityManager);

		Light sun = new Light(new Vector3f(10000, 10000, 10000), new Vector3f(1, 1, 0), new Vector3f(0, 0, 0));
		scene.addLight(sun);
		gameFactory.setSkybox(scene);
	}

	int counter;

	@Override
	public void processInputs() {
		if (KeyboardHandler.kb_keyDownOnce(KEY_JOIN)) {
			counter++;
			System.out.println(counter);
			eventHandler.addEvent(new Event(EventType.JOIN_SERVER, "localhost", config.gameServerDefaultPort));
		}
		if (KeyboardHandler.kb_keyDownOnce(KEY_DISCONNECT)) {
			eventHandler.addEvent(new Event(EventType.DISCONNECT));
		}
	}

	long timer = System.currentTimeMillis();
	boolean create;
	int frequency = 200;

	@Override
	public void update(float dt) {
		if (System.currentTimeMillis() - timer >= frequency) {
			timer += frequency;
			if (create) {
				dragon = gameFactory.createDragon();
				scene.attach(dragon);
			} else {
				entityManager.removeEntity(dragon);
			}
			create = !create;
		}
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void close() {
	}

}
