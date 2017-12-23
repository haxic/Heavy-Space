package client.controllers;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.components.SnapshotComponent;
import client.gameData.ClientGameFactory;
import client.gameData.GameModel;
import client.gameData.GameAssetLoader;
import client.gameData.Scene;
import client.inputs.KeyboardHandler;
import client.inputs.ShipControls;
import client.network.ConnectionManager;
import client.systems.DeathSystem;
import client.systems.SnapshotSystem;
import client.systems.SpawnSystem;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.DeathComponent;
import shared.components.ObjectComponent;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.systems.MovementSystem;
import shared.systems.ProjectileSystem;

public class GameController implements IController {
	private static final int KEY_TOGGLE_SNAPSHOT_INTERPOLATION = GLFW.GLFW_KEY_I;
	private static final int TURBO = GLFW.GLFW_KEY_SPACE;
	private static final int SPAWN_SHIP = GLFW.GLFW_KEY_T;

	private Scene scene;

	// private EventHandler eventHandler;
	private ClientGameFactory clientGameFactory;
	private EntityManager entityManager;
	private ConnectionManager connectionManager;
	private GameModel gameModel;

	boolean useSnapshotInterpolation;

	private MovementSystem movementSystem;
	// private CollisionSystem collisionSystem;
	private SpawnSystem spawnSystem;
	private SnapshotSystem snapshotSystem;
	private ProjectileSystem projectileSystem;
	private DeathSystem deathSystem;

	private ShipControls shipControls;

	public GameController(EventHandler eventHandler, ConnectionManager connectionManager, GameAssetLoader gameAssetLoader) {
		this.connectionManager = connectionManager;
		entityManager = new EntityManager();
		clientGameFactory = new ClientGameFactory(entityManager, gameAssetLoader);

		gameModel = new GameModel(entityManager);
		shipControls = new ShipControls();
		useSnapshotInterpolation = true;

		scene = new Scene(entityManager);

		movementSystem = new MovementSystem(entityManager);
		// collisionSystem = new CollisionSystem(entityManager);
		projectileSystem = new ProjectileSystem(entityManager);
		spawnSystem = new SpawnSystem(entityManager, scene, connectionManager, eventHandler, clientGameFactory);
		snapshotSystem = new SnapshotSystem(entityManager);
		deathSystem = new DeathSystem(entityManager, gameModel);

		clientGameFactory.setSkybox(scene);
		Entity sun1 = clientGameFactory.createSun(new Vector3f(0, 5000, -10000), new Vector3f(1, 1, 1));
		scene.addLightEntity(sun1);
		Entity sun2 = clientGameFactory.createSun(new Vector3f(-7000, -7000, 7000), new Vector3f(1, 1, 1));
		scene.addLightEntity(sun2);

		clientGameFactory.createLotsOfDebris(scene, 1000);
	}

	Vector3f linearDirection = new Vector3f();
	Vector3f angularDirection = new Vector3f();

	@Override
	public void processInputs() {
		shipControls.process();
		if (KeyboardHandler.kb_keyDownOnce(KEY_TOGGLE_SNAPSHOT_INTERPOLATION))
			useSnapshotInterpolation = !useSnapshotInterpolation;
	}

	Entity shipEntity;

	private final int timestep = 15 * 3;
	private final float timestepDT = timestep / 1000.0f;
	private float timestepCounter;

	int tick;

	@Override
	public void update(float dt) {
		float ssdt = 0;
		if (running) {
			timestepCounter += dt;
			while (timestepCounter > timestepDT) {
				timestepCounter -= timestepDT;
				tick = (short) (++tick % Short.MAX_VALUE);
			}
			ssdt = timestepCounter / timestepDT;
		}

		// velocity.set(scene.camera.getForward().mul(Globals.dt * currentSpeed * speeder * shipControls.getLinearDirection().z, tempVector));
		// velocity.add(scene.camera.right.mul(Globals.dt * currentSpeed * speeder * shipControls.getLinearDirection().x, tempVector));
		// velocity.add(scene.camera.up.mul(Globals.dt * currentSpeed * speeder * shipControls.getLinearDirection().y, tempVector));
		//
		// scene.camera.position.add(velocity);
		//
		// scene.camera.yaw(Globals.dt * shipControls.getAngularVelocity().y);
		// scene.camera.pitch(Globals.dt * shipControls.getAngularVelocity().x);
		// scene.camera.roll(Globals.dt * shipControls.getAngularVelocity().z);

		if (running) {

			if (KeyboardHandler.kb_keyDownOnce(SPAWN_SHIP))
				connectionManager.requestSpawnShip(tick, scene.getCamera().position);
			if (shipEntity != null)
				connectionManager.sendShipActions(tick, dt, shipControls);

			spawnSystem.process(tick);
			snapshotSystem.process(ssdt, tick, useSnapshotInterpolation);
			movementSystem.process(dt);
			deathSystem.process();
			projectileSystem.process(dt);
		}

		// if (shipEntity != null) {
		// System.out.println(shipEntity.getEID());
		// }
		ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(shipEntity, ObjectComponent.class);
		if (object != null) {
			// System.out.println("GameControl: " + object.getForward().x + " " + object.getForward().y + " " + object.getForward().z);
			scene.getCamera().getPosition().set(object.getPosition());
			scene.getCamera().getForward().set(object.getForward());
			scene.getCamera().getUp().set(object.getUp());
			scene.getCamera().getRight().set(object.getRight());
		}
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void close() {
	}

	public void spawnPlayerShipFromEvent(Event event) {
		shipEntity = (Entity) event.data[0];
		ObjectComponent object = (ObjectComponent) entityManager.getComponentInEntity(shipEntity, ObjectComponent.class);
		scene.getCamera().getPosition().set((Vector3f) object.getPosition());
	}

	public void createEntityFromEvent(Event event) {
		checkTick(clientGameFactory.createEntityFromEvent(event, gameModel));
	}

	public void updateUnitFromEvent(Event event) {
		// eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_UPDATE_UNIT, tick, eeid, flags, position, forward, up, killingEeid));

		short tick = (short) event.data[0];
		if (!checkTick(tick))
			return;
		int eeid = (int) event.data[1];
		Entity entity = gameModel.getEntity(eeid);
		if (entity == null)
			return;
		boolean[] flags = (boolean[]) event.data[2];
		if (flags[0]) {
			Vector3f position = (Vector3f) event.data[3];
			Quaternionf orientation = (Quaternionf) event.data[4];
			SnapshotComponent snapshotComponent = (SnapshotComponent) entityManager.getComponentInEntity(entity, SnapshotComponent.class);
			snapshotComponent.add(tick, position, orientation);
		} else {
			// System.out.println("UPDATE WITHOUT SNAPSHOT " + eeid);
		}
		if (flags[1]) {
			DeathComponent deathComponent = null;
			if (flags[2]) {
				int killingEEID = (int) event.data[5];
				Entity killingEntity = gameModel.getEntity(killingEEID);
				if (killingEntity != null) {
					deathComponent = new DeathComponent(tick, killingEntity);
					entityManager.addComponent(new DeathComponent(tick), killingEntity);
				} else {
					deathComponent = new DeathComponent(tick);
				}
			} else {
				deathComponent = new DeathComponent(tick);
			}
			entityManager.addComponent(deathComponent, entity);
		}
	}

	public void updateSnapshotFromEvent(Event event) {
		short tick = (short) event.data[0];
		checkTick(tick);
	}

	boolean running = false;
	Short firstTick = null;
	Short lastTick = null;

	private boolean checkTick(short checkedTick) {
		if (!running) {
			if (firstTick == null)
				firstTick = checkedTick;
			else if (checkedTick < firstTick)
				firstTick = checkedTick;
			if (lastTick == null)
				lastTick = checkedTick;
			else if (checkedTick > lastTick)
				lastTick = checkedTick;
			int diff = lastTick - firstTick;
			if (diff > 3) {
				tick = (short) (lastTick - 2);
				running = true;
				System.out.println("RUN");
			} else {
				tick = lastTick;
			}
		}

		int tickDiff;
		// || (newTick < 1000 && latestTick > 8000
		if (checkedTick > tick && !(checkedTick > 8000 && tick < 1000) || tick > checkedTick  && !(tick  > 8000 && checkedTick < 1000))
			tickDiff = tick - checkedTick;
		else
			tickDiff = 0;

		// Ignore ticks that are too far away from the current tick
		if (tickDiff < -8 || tickDiff > 8)
			return false;

		return true;
	}

	@Override
	public int getTick() {
		return tick;
	}

}
