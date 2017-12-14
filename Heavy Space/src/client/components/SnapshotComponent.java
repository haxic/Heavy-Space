package client.components;

import java.util.Deque;
import java.util.LinkedList;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import client.gameData.Snapshot;
import hecs.EntityComponent;
import shared.functionality.Globals;

public class SnapshotComponent extends EntityComponent {

	short playerID;
	Deque<Snapshot> snapshots;
	Snapshot current;
	Snapshot next;

	public SnapshotComponent(short playerID, short tick, Vector3f position, Vector3f forward, Vector3f up, Vector3f right) {
		this.playerID = playerID;
		snapshots = new LinkedList();
		current = new Snapshot(tick, position, forward, up, right);
		next = current;
	}

	@Override
	protected void removeComponent() {
	}

	public short getPlayerID() {
		return playerID;
	}

	public Snapshot next() {
		current = next;
		next = snapshots.pollFirst();
		return current;
	}

	public Snapshot peekNext() {
		return snapshots.peekFirst();
	}

	public void add(short tick, Vector3f position, Vector3f forward, Vector3f up, Vector3f right) {
		Snapshot next = snapshots.peekLast();
		// Add snapshot if new tick is after latest tick or if new tick is in minimum bracket while latest tick is in maximum bracket
		if (isAfter(tick, current.getTick()) && (next == null || isAfter(tick, next.getTick())))
			snapshots.addLast(new Snapshot(tick, position, forward, up, right));
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

	public Snapshot peekLatest() {
		return snapshots.peekLast();
	}
}
