package client.systems;

import java.util.List;

import org.joml.Vector3f;

import client.components.SnapshotComponent;
import client.gameData.Snapshot;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.ObjectComponent;
import shared.functionality.Globals;

public class SnapshotSystem {

	private EntityManager entityManager;
	private Vector3f tempVector = new Vector3f();

	public SnapshotSystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void process(float dt, boolean useSnapshotInterpolation) {
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
			if (current.getTick() == Globals.tick) {
				interpolate(dt, unitComponent, snapshotComponent);
			} else if (current.getTick() < Globals.tick) {
				if (next.getTick() > Globals.tick) {
					interpolate(dt, unitComponent, snapshotComponent);
				} else if (snapshotComponent.peekNext() != null) {
					// Interpolating on next set
					current = snapshotComponent.next();
					next = snapshotComponent.getNext();

					interpolate(dt, unitComponent, snapshotComponent);
				} else {
					// Extrapolate using current and next
				}
			} else if (current.getTick() > Globals.tick) {
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
		}
	}

	private void interpolate(float dt, ObjectComponent unitComponent, SnapshotComponent snapshotComponent) {
		// TODO: figure out why this is here?
		snapshotComponent.getDifference();
		snapshotComponent.getNext().getPosition().sub(snapshotComponent.getCurrent().getPosition(), tempVector);
		unitComponent.getPosition().set(snapshotComponent.getCurrent().getPosition()).fma(dt, tempVector);
		
		snapshotComponent.getNext().getForward().sub(snapshotComponent.getCurrent().getForward(), tempVector);
		unitComponent.getForward().set(snapshotComponent.getCurrent().getForward()).fma(dt, tempVector);
		
		snapshotComponent.getNext().getUp().sub(snapshotComponent.getCurrent().getUp(), tempVector);
		unitComponent.getUp().set(snapshotComponent.getCurrent().getUp()).fma(dt, tempVector);
		
		snapshotComponent.getNext().getRight().sub(snapshotComponent.getCurrent().getRight(), tempVector);
		unitComponent.getRight().set(snapshotComponent.getCurrent().getRight()).fma(dt, tempVector);
	}

}
