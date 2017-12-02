package client.main;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.components.Snapshot;
import client.components.SnapshotComponent;
import client.display.DisplayManager;
import client.inputs.KeyboardHandler;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.UnitComponent;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.functionality.Globals;

public class GameController implements ClientController {
	private Scene scene;

	private EventHandler eventHandler;
	private GameFactory gameFactory;
	private EntityManager entityManager;
	private GameModel gameModel;

	private static final int KEY_SPAWN = GLFW.GLFW_KEY_SPACE;

	public GameController(EntityManager entityManager, EventHandler eventHandler, GameFactory gameFactory) {
		this.entityManager = entityManager;
		this.eventHandler = eventHandler;
		this.gameFactory = gameFactory;
		this.gameModel = new GameModel(entityManager);
		scene = new Scene(entityManager);
		gameFactory.setSkybox(scene);
	}

	float mouseSpeed = 0.25f;
	float speed = 50f;
	float currentSpeed = speed;
	float rollSpeed = 2f;
	int invertX = 1;
	int invertY = -1;

	@Override
	public void processInputs() {
		Vector2f mousePositionDelta = DisplayManager.getMousePositionDelta();
		float hor = invertX * mouseSpeed * Globals.dt * mousePositionDelta.x;
		float ver = invertY * mouseSpeed * Globals.dt * mousePositionDelta.y;
		if (!DisplayManager.isCursorEnabled()) {
			scene.camera.pitch(ver);
			scene.camera.yaw(hor);
		}
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_SPACE)) {
			currentSpeed = speed * 4;
		}
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_W))
			scene.camera.position.add(scene.camera.getForward().mul(Globals.dt * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_S))
			scene.camera.position.sub(scene.camera.getForward().mul(Globals.dt * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_D))
			scene.camera.position.add(scene.camera.right.mul(Globals.dt * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_A))
			scene.camera.position.sub(scene.camera.right.mul(Globals.dt * currentSpeed, new Vector3f()));

		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_Q))
			scene.camera.roll(Globals.dt * rollSpeed);
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_E))
			scene.camera.roll(-Globals.dt * rollSpeed);

		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			scene.camera.position.add(scene.camera.up.mul(Globals.dt * currentSpeed, new Vector3f()));
		if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_SHIFT))
			scene.camera.position.sub(scene.camera.up.mul(Globals.dt * currentSpeed, new Vector3f()));

		if (KeyboardHandler.kb_keyDownOnce(KEY_SPAWN)) {
			eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_GAME_ACTION_SPAWN));
		}
	}

	private final float timestep = 100 / 1000.0f;
	private final float timestepDT = 1.0f / timestep;

	float timestepCounter;

	Vector3f tempVector = new Vector3f();

	@Override
	public void update() {
		if (!running)
			return;
		timestepCounter += Globals.dt;
		while (timestepCounter > timestep) {
			timestepCounter -= timestep;
			Globals.tick++;
		}

		float dt = 1 - (timestepCounter / timestep);

		// We are at tick 211 + 10ms
		// 211+10ms-100ms = 211-90ms = 211-(90/15) = 211-6 = tick 205 + 0ms
		// 211+13ms-100ms = 211-87ms = 211-(87/15) = 211-5+12ms = tick 206 + 12ms

		List<Entity> entities = entityManager.getEntitiesContainingComponent(SnapshotComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			UnitComponent unitComponent = (UnitComponent) entityManager.getComponentInEntity(entity, UnitComponent.class);
			SnapshotComponent snapshotComponent = (SnapshotComponent) entityManager.getComponentInEntity(entity, SnapshotComponent.class);

			Snapshot current = snapshotComponent.getCurrent();
			Snapshot next = snapshotComponent.getNext();
			if (current.getTick() == Globals.tick) {
				// Continue interpolating between current and next
				snapshotComponent.getDifference();
				current.getPosition().sub(next.getPosition(), tempVector);
				unitComponent.getPosition().set(current.getPosition()).fma(dt, tempVector);

			} else if (current.getTick() < Globals.tick) {
				if (next.getTick() > Globals.tick) {
					// Continue interpolating between current and next
					snapshotComponent.getDifference();
					current.getPosition().sub(next.getPosition(), tempVector);
					unitComponent.getPosition().set(current.getPosition()).fma(dt, tempVector);
				} else if (snapshotComponent.peekNext() != null) {
					// Interpolating on next set
					current = snapshotComponent.next();
					next = snapshotComponent.getNext();

					snapshotComponent.getDifference();
					current.getPosition().sub(next.getPosition(), tempVector);
					unitComponent.getPosition().set(current.getPosition()).fma(dt, tempVector);
				} else {
					// Extrapolate using current and next
				}
			} else if (current.getTick() > Globals.tick) {
				unitComponent.getPosition().set(current.getPosition());
			}
		}
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
		gameModel.addEntity(eeid, entity);
		scene.addEntity(entity);
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
//		System.out.println("(" + tick + ") Updated: " + eeid);
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
