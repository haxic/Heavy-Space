package client.main;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.components.SnapshotComponent;
import client.entities.Light;
import client.inputs.KeyboardHandler;
import client.network.ConnectionManager;
import client.systems.SnapshotSystem;
import client.systems.SpawnSystem;
import hecs.Entity;
import hecs.EntityManager;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.Globals;
import shared.systems.CollisionSystem;
import shared.systems.MovementSystem;
import shared.systems.ProjectileSystem;

public class GameController implements ClientController {
	private static final int KEY_TOGGLE_SNAPSHOT_INTERPOLATION = GLFW.GLFW_KEY_I;
	private static final int TURBO = GLFW.GLFW_KEY_SPACE;
	private static final int SPAWN_SHIP = GLFW.GLFW_KEY_T;

	private Scene scene;

	private EventHandler eventHandler;
	private ClientGameFactory clientGameFactory;
	private EntityManager entityManager;
	private ConnectionManager connectionManager;
	private GameModel gameModel;

	boolean useSnapshotInterpolation;

	private MovementSystem movementSystem;
	private CollisionSystem collisionSystem;
	private SpawnSystem spawnSystem;
	private SnapshotSystem snapshotSystem;
	private ProjectileSystem projectileSystem;

	private ShipControls shipControls;

	public GameController(EntityManager entityManager, EventHandler eventHandler, ClientGameFactory clientGameFactory, ConnectionManager connectionManager) {
		this.entityManager = entityManager;
		this.eventHandler = eventHandler;
		this.clientGameFactory = clientGameFactory;
		this.connectionManager = connectionManager;

		gameModel = new GameModel(entityManager);
		shipControls = new ShipControls();
		useSnapshotInterpolation = true;

		scene = new Scene(entityManager);

		movementSystem = new MovementSystem(entityManager);
		collisionSystem = new CollisionSystem(entityManager);
		projectileSystem = new ProjectileSystem(entityManager);
		spawnSystem = new SpawnSystem(entityManager, scene);
		snapshotSystem = new SnapshotSystem(entityManager);

		clientGameFactory.setSkybox(scene);
		Light sun = new Light(new Vector3f(10000, 10000, 10000), new Vector3f(1, 1, 0), new Vector3f(0, 0, 0));
		scene.addLight(sun);
	}

	Vector3f linearDirection = new Vector3f();
	Vector3f angularDirection = new Vector3f();

	@Override
	public void processInputs() {
		shipControls.process();
		if (KeyboardHandler.kb_keyDownOnce(KEY_TOGGLE_SNAPSHOT_INTERPOLATION))
			useSnapshotInterpolation = !useSnapshotInterpolation;
	}

	private final int timestep = 100;
	private final float timestepDT = timestep / 1000.0f;
	private float timestepCounter;

	float speed = 50f;
	float currentSpeed = speed;
	Vector3f tempVector = new Vector3f();

	@Override
	public void update() {
		float speeder = 1;
		if (KeyboardHandler.kb_keyDown(TURBO))
			speeder = 10;

		scene.camera.position.add(scene.camera.getForward().mul(Globals.dt * currentSpeed * speeder * shipControls.getLinearDirection().z, tempVector));
		scene.camera.position.add(scene.camera.right.mul(Globals.dt * currentSpeed * speeder * shipControls.getLinearDirection().x, tempVector));
		scene.camera.position.add(scene.camera.up.mul(Globals.dt * currentSpeed * speeder * shipControls.getLinearDirection().y, tempVector));

		scene.camera.yaw(Globals.dt * shipControls.getAngularVelocity().y);
		scene.camera.pitch(Globals.dt * shipControls.getAngularVelocity().x);
		scene.camera.roll(Globals.dt * shipControls.getAngularVelocity().z);

		movementSystem.process();
		collisionSystem.process();
		projectileSystem.process();

		if (!running)
			return;

		if (KeyboardHandler.kb_keyDownOnce(SPAWN_SHIP))
			connectionManager.requestSpawnShip(scene.camera.position);
		connectionManager.sendShipActions(shipControls.getFirePrimary(), scene.camera.position, scene.camera.getForward());

		timestepCounter += Globals.dt;
		while (timestepCounter > timestepDT) {
			timestepCounter -= timestepDT;
			Globals.tick++;
		}
		float dt = timestepCounter / timestepDT;

		spawnSystem.process();
		snapshotSystem.process(dt, useSnapshotInterpolation);
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void close() {
	}

	public void createEntityFromEvent(Event event) {
		clientGameFactory.createEntityFromEvent(event, gameModel);
	}

	public void updateUnitFromEvent(Event event) {
		short tick = (short) event.data[0];
		if (!checkTick(tick))
			return;
		int eeid = (int) event.data[1];
		Entity entity = gameModel.getEntity(eeid);
		if (entity == null)
			return;
		Vector3f position = (Vector3f) event.data[2];
		// System.out.println("(" + tick + ") Updated: " + eeid);
		SnapshotComponent snapshotComponent = (SnapshotComponent) entityManager.getComponentInEntity(entity, SnapshotComponent.class);
		snapshotComponent.add(tick, position);
	}

	boolean running = false;
	Short firstTick = null;
	Short lastTick = null;

	private boolean checkTick(short tick) {
		if (!running) {
			if (firstTick == null)
				firstTick = tick;
			else if (tick < firstTick)
				firstTick = tick;
			if (lastTick == null)
				lastTick = tick;
			else if (tick > lastTick)
				lastTick = tick;
			int diff = lastTick - firstTick;
			System.out.println("CHECK FOR TICK: " + diff);
			if (diff > 3) {
				Globals.tick = (short) (lastTick - 2);
				running = true;
				System.out.println(Globals.tick + " " + timestepCounter);
			} else {
				Globals.tick = lastTick;
			}
		}

		int tickDiff;

		// If new tick is in min bracket and current tick in max bracket
		if (tick < Short.MIN_VALUE / 2 && Globals.tick > Short.MAX_VALUE / 2)
			tickDiff = (Short.MAX_VALUE - Globals.tick) + (tick - Short.MIN_VALUE);
		else
			tickDiff = Globals.tick - tick;

		// Ignore ticks that are too far away from the current tick
		if (tickDiff < -8 || tickDiff > 8)
			return false;

		return true;
	}
}
