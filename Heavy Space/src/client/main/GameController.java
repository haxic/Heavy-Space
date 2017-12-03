package client.main;

import java.net.DatagramPacket;

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
import shared.components.SpawnComponent;
import shared.functionality.DataPacket;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.Globals;
import shared.functionality.network.RequestType;

public class GameController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private GameFactory gameFactory;
	private EntityManager entityManager;
	private ConnectionManager connectionManager;
	private GameModel gameModel;

	private static final int KEY_TOGGLE_SNAPSHOT_INTERPOLATION = GLFW.GLFW_KEY_I;

	boolean useSnapshotInterpolation;

	private SpawnSystem spawnSystem;
	private SnapshotSystem snapshotSystem;

	private ShipControls shipControls;

	public GameController(EntityManager entityManager, EventHandler eventHandler, GameFactory gameFactory, ConnectionManager connectionManager) {
		this.entityManager = entityManager;
		this.eventHandler = eventHandler;
		this.gameFactory = gameFactory;
		this.connectionManager = connectionManager;

		gameModel = new GameModel(entityManager);
		shipControls = new ShipControls();
		useSnapshotInterpolation = true;

		scene = new Scene(entityManager);

		spawnSystem = new SpawnSystem(entityManager, scene);
		snapshotSystem = new SnapshotSystem(entityManager);

		gameFactory.setSkybox(scene);
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

	private final float timestep = 100 / 1000.0f;
	private float timestepCounter;

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

		if (shipControls.getFirePrimary()) {
			connectionManager.sendControlShip();
		}

		if (!running)
			return;
		timestepCounter += Globals.dt;
		while (timestepCounter > timestep) {
			timestepCounter -= timestep;
			Globals.tick++;
		}
		float dt = timestepCounter / timestep;

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

	public void createUnitFromEvent(Event event) {
		short tick = (short) event.data[0];
		int eeid = (int) event.data[1];
		int entityType = (int) event.data[2];
		int entityVariation = (int) event.data[3];
		Vector3f position = (Vector3f) event.data[4];
		Entity entity = gameModel.getEntity(eeid);
		if (entity != null)
			return;
		// TODO: Check if eeid already exist!
		System.out.println("Entity created: " + eeid + " " + entityType + " " + entityVariation + " " + position);
		entity = gameFactory.createShip(position);
		entityManager.addComponent(new SnapshotComponent(eeid, tick, position), entity);
		entityManager.addComponent(new SpawnComponent(tick), entity);
		gameModel.addEntity(eeid, entity);
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
				Globals.tick = (short) (lastTick - 3);
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
