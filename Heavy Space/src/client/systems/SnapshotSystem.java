package client.systems;

import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import client.components.SnapshotComponent;
import client.gameData.Snapshot;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.ObjectComponent;

public class SnapshotSystem {

	private EntityManager entityManager;
	private Vector3f tempVector = new Vector3f();

	public SnapshotSystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void process(float dt, int tick, boolean useSnapshotInterpolation) {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(SnapshotComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			ObjectComponent unitComponent = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
			SnapshotComponent snapshotComponent = (SnapshotComponent) entityManager.getComponentInEntity(entity, SnapshotComponent.class);

			Snapshot current = snapshotComponent.getCurrent();
			Snapshot next = snapshotComponent.getNext();

			if (current.getTick() == tick) {
				interpolate(dt, unitComponent, snapshotComponent);
			} else if (current.getTick() < tick || (tick < 1000 && current.getTick() > 8000)) {
				if ((next.getTick() > tick && !(next.getTick() > 8000 && tick < 1000)) || (next.getTick() < 1000 && tick > 8000)) {
					interpolate(dt, unitComponent, snapshotComponent);
				} else if (snapshotComponent.peekNext() != null) {
					// Interpolating on next set
					snapshotComponent.next();

					interpolate(dt, unitComponent, snapshotComponent);
				} else {
					// Extrapolate using current and next
				}
			} else {
				unitComponent.getPosition().set(current.getPosition());
			}
			if (!useSnapshotInterpolation) {
				Vector3f latestPosition;
				Snapshot latestSnapshot = snapshotComponent.peekLatest();
				if (latestSnapshot == null)
					latestPosition = snapshotComponent.getNext().getPosition();
				else
					latestPosition = latestSnapshot.getPosition();
				unitComponent.getPosition().set(latestPosition);
			}
			// System.out.println(unitComponent.getPosition().x + " , " + Globals.tick + " , " + dt);
		}
	}

	Quaternionf tempQuaternion = new Quaternionf();
	private void interpolate(float dt, ObjectComponent unitComponent, SnapshotComponent snapshotComponent) {
		// TODO: figure out why this is here?
		snapshotComponent.getDifference();
		snapshotComponent.getNext().getPosition().sub(snapshotComponent.getCurrent().getPosition(), tempVector);
		unitComponent.getPosition().set(snapshotComponent.getCurrent().getPosition()).fma(dt, tempVector);

		snapshotComponent.getCurrent().getOrientation().slerp(snapshotComponent.getNext().getOrientation(), dt, unitComponent.getOrientation());
		unitComponent.updateOrientation();
	}

}
