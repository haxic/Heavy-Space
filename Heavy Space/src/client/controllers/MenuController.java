package client.controllers;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.components.LightComponent;
import client.display.DisplayManager;
import client.gameData.ClientGameFactory;
import client.gameData.GameAssetLoader;
import client.gameData.Scene;
import client.inputs.KeyboardHandler;
import client.inputs.ShipControls;
import client.network.GameServerData;
import gameServer.systems.ShipSystem;
import hecs.Entity;
import hecs.EntityManager;
import hevent.EventManager;
import shared.components.ObjectComponent;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.systems.AIBotSystem;
import shared.systems.CollisionSystem;
import shared.systems.MovementSystem;

public class MenuController implements IController {
	private Scene scene;

	private EventHandler eventHandler;
	private ClientGameFactory clientGameFactory;
	private EntityManager entityManager;

	private GameServerData gameServerData;

	private static final int KEY_DISCONNECT = GLFW.GLFW_KEY_ESCAPE;
	private static final int KEY_JOIN = GLFW.GLFW_KEY_C;
	private static final int KEY_TOGGLE_MOUSE = GLFW.GLFW_KEY_M;

	private ShipControls shipControls;

	Entity dragon;

	private AIBotSystem aiBotSystem;
	private ShipSystem shipSystem;
	private MovementSystem movementSystem;
	private CollisionSystem collisionSystem;
	ShipControls controls = new ShipControls();
	EventManager eventManager;
	public MenuController(EventHandler eventHandler, GameServerData gameServerData, GameAssetLoader gameAssetLoader) {
		this.eventHandler = eventHandler;
		this.gameServerData = gameServerData;
		entityManager = new EntityManager();
		clientGameFactory = new ClientGameFactory(entityManager, gameAssetLoader);

		shipControls = new ShipControls();
		eventManager = new EventManager();
		aiBotSystem = new AIBotSystem(entityManager);
		shipSystem = new ShipSystem(entityManager, null, null);
		movementSystem = new MovementSystem(entityManager);
		collisionSystem = new CollisionSystem(entityManager);

		scene = new Scene(entityManager);
		clientGameFactory.setSkybox(scene);

		Entity sun = clientGameFactory.createSun(new Vector3f(1000, 1000, 1000), new Vector3f(1, 1, 0));
		scene.addLightEntity(sun);
		clientGameFactory.createLotsOfDebris(scene, 1000);
		// scene.addEntity(gameFactory.createBot(new Vector3f(0, -20, -10), 10f, 1f));
		// scene.addEntity(gameFactory.createBot(new Vector3f(-20, 0, -20),15f, -1f));
		// scene.addEntity(gameFactory.createBot(new Vector3f(0, 0, 0), 10f, 1f));
		// scene.addEntity(gameFactory.createBot(new Vector3f(0, 20, 10), 15f, -1f));
		scene.addActorEntity(clientGameFactory.createBot(new Vector3f(50, 50, 20), 1f, 1f));
		test = clientGameFactory.createBox(new Vector3f(0, 0, -8));
		scene.addActorEntity(test);
	}
	Entity test;
	@Override
	public void processInputs() {
		// if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_LEFT_ALT))
		// DisplayManager.toggleCursor();
		if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_Z))
			scene.getCamera().toggleLockUp();

		if (KeyboardHandler.kb_keyDownOnce(KEY_JOIN)) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_JOIN, gameServerData));
		}
		if (KeyboardHandler.kb_keyDownOnce(KEY_DISCONNECT)) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_DISCONNECT));
		}
		if (KeyboardHandler.kb_keyDownOnce(KEY_TOGGLE_MOUSE)) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_TOGGLE_MOUSE));
		}
		shipControls.process();
		scene.getCamera().yaw(shipControls.angularDirection.y * 0.1f);
		scene.getCamera().pitch(shipControls.angularDirection.x * 0.1f);
		ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(test, ObjectComponent.class);
//		object.rotate(0.01f, 0.02f, 0.03f);
//		object.rotate(shipControls.angularDirection.y * Globals.dt, shipControls.angularDirection.x * Globals.dt, 0.0000f);
		
	}

	long timer = System.currentTimeMillis();
	boolean create;
	int frequency = 200;

	float speed = 50f;
	float currentSpeed = speed;
	Vector3f tempVector = new Vector3f();

	@Override
	public void update(float dt) {
		// scene.camera.position.add(scene.camera.getForward().mul(Globals.dt * currentSpeed * shipControls.getLinearDirection().z, tempVector));
		// scene.camera.position.add(scene.camera.right.mul(Globals.dt * currentSpeed * shipControls.getLinearDirection().x, tempVector));
		// scene.camera.position.add(scene.camera.up.mul(Globals.dt * currentSpeed * shipControls.getLinearDirection().y, tempVector));
		//
		// scene.camera.yaw(Globals.dt * shipControls.getAngularVelocity().y);
		// scene.camera.pitch(Globals.dt * shipControls.getAngularVelocity().x);
		// scene.camera.roll(Globals.dt * shipControls.getAngularVelocity().z);
		// if (System.currentTimeMillis() - timer >= frequency) {
		// timer += frequency;
		// if (create) {
		// dragon = gameFactory.createDragon();
		// scene.attach(dragon);
		// } else {
		// entityManager.removeEntity(dragon);
		// }
		// create = !create;
		// }
		aiBotSystem.update(dt);
		shipSystem.process(dt);
		movementSystem.process(dt);
		collisionSystem.process();
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void close() {
	}

	@Override
	public int getTick() {
		return 0;
	}

}
