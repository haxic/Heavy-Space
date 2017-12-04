package client.main;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.entities.Light;
import client.inputs.KeyboardHandler;
import client.network.GameServerData;
import hecs.Entity;
import hecs.EntityManager;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.functionality.Globals;
import shared.systems.AIBotSystem;
import shared.systems.CollisionSystem;
import shared.systems.MovementSystem;

public class MenuController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private ClientGameFactory clientGameFactory;
	private EntityManager entityManager;

	private GameServerData gameServerData;

	private static final int KEY_DISCONNECT = GLFW.GLFW_KEY_ESCAPE;
	private static final int KEY_JOIN = GLFW.GLFW_KEY_C;

	private ShipControls shipControls;

	Entity dragon;

	private AIBotSystem aiBotSystem;
	private MovementSystem movementSystem;
	private CollisionSystem collisionSystem;
	
	public MenuController(EntityManager entityManager, EventHandler eventHandler, ClientGameFactory clientGameFactory, GameServerData gameServerData) {
		this.entityManager = entityManager;
		this.eventHandler = eventHandler;
		this.clientGameFactory = clientGameFactory;
		this.gameServerData = gameServerData;

		shipControls = new ShipControls();

		aiBotSystem = new AIBotSystem(entityManager);
		movementSystem = new MovementSystem(entityManager);
		collisionSystem = new CollisionSystem(entityManager);
		
		scene = new Scene(entityManager);
		Light sun = new Light(new Vector3f(10000, 10000, 10000), new Vector3f(1, 1, 0), new Vector3f(0, 0, 0));
		scene.addLight(sun);
		clientGameFactory.setSkybox(scene);

//		scene.addEntity(gameFactory.createBot(new Vector3f(0, -20, -10), 10f, 1f));
//		scene.addEntity(gameFactory.createBot(new Vector3f(-20, 0, -20),15f, -1f));
//		scene.addEntity(gameFactory.createBot(new Vector3f(0, 0, 0), 10f, 1f));
//		scene.addEntity(gameFactory.createBot(new Vector3f(0, 20, 10), 15f, -1f));
		scene.addEntity(clientGameFactory.createBot(new Vector3f(20, 0, 20), 25f, 1f));
	}

	@Override
	public void processInputs() {
		// if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_LEFT_ALT))
		// DisplayManager.toggleCursor();
		if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_Z))
			scene.camera.toggleLockUp();

		if (KeyboardHandler.kb_keyDownOnce(KEY_JOIN)) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_JOIN, gameServerData));
		}
		if (KeyboardHandler.kb_keyDownOnce(KEY_DISCONNECT)) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SERVER_DISCONNECT));
		}
		shipControls.process();
	}

	long timer = System.currentTimeMillis();
	boolean create;
	int frequency = 200;

	float speed = 50f;
	float currentSpeed = speed;
	Vector3f tempVector = new Vector3f();

	@Override
	public void update() {
		scene.camera.position.add(scene.camera.getForward().mul(Globals.dt * currentSpeed * shipControls.getLinearDirection().z, tempVector));
		scene.camera.position.add(scene.camera.right.mul(Globals.dt * currentSpeed * shipControls.getLinearDirection().x, tempVector));
		scene.camera.position.add(scene.camera.up.mul(Globals.dt * currentSpeed * shipControls.getLinearDirection().y, tempVector));

		scene.camera.yaw(Globals.dt * shipControls.getAngularVelocity().y);
		scene.camera.pitch(Globals.dt * shipControls.getAngularVelocity().x);
		scene.camera.roll(Globals.dt * shipControls.getAngularVelocity().z);
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
		aiBotSystem.update();
		movementSystem.process();
		collisionSystem.process();
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void close() {
	}

}
