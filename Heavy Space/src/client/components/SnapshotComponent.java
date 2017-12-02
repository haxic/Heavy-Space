package client.components;

import java.util.Deque;
import java.util.LinkedList;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import hecs.EntityComponent;
import shared.functionality.Globals;

public class SnapshotComponent extends EntityComponent {

	int eeid;
	Deque<Snapshot> snapshots;
	Snapshot current;
	Snapshot next;

	public SnapshotComponent(int eeid, short tick, Vector3f position) {
		this.eeid = eeid;
		snapshots = new LinkedList();
		current = new Snapshot(tick, position);
		next = current;
	}

	@Override
	protected void removeComponent() {
	}

	public Snapshot next() {
		current = next;
		next = snapshots.pollFirst();
		return current;
	}

	public Snapshot peekNext() {
		return snapshots.peekFirst();
	}

	public void add(short tick, Vector3f position) {
		Snapshot next = snapshots.peekLast();
		// Add snapshot if new tick is after latest tick or if new tick is in minimum bracket while latest tick is in maximum bracket
		if (isAfter(tick, current.tick) && (next == null || isAfter(tick, next.tick)))
			snapshots.addLast(new Snapshot(tick, position));
	}

	public boolean isAfter(short newTick, short latestTick) {
		return newTick > latestTick || (newTick < latestTick && newTick < (Short.MIN_VALUE / 2) && latestTick > (Short.MAX_VALUE / 2));
	}

	public Snapshot getCurrent() {
		return current;
	}

	public Snapshot getNext() {
		return next;
	}

	public int getDifference() {
		if (current.getTick() < Short.MIN_VALUE / 2 && next.getTick() > Short.MAX_VALUE / 2)
			return (Short.MAX_VALUE - next.getTick()) + (current.getTick() - Short.MIN_VALUE);
		else
			return next.getTick() - current.getTick();
	}

	public int getEEID() {
		return eeid;
	}

	public Snapshot peekLatest() {
		return snapshots.peekLast();
	}
}
