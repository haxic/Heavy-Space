package client.components;

import java.util.Deque;
import java.util.LinkedList;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import client.gameData.Snapshot;
import hecs.EntityComponent;

public class SnapshotComponent extends EntityComponent {

	short playerID;
	Deque<Snapshot> snapshots;
	Snapshot current;
	Snapshot next;

//	public SnapshotComponent(short playerID, short tick, Vector3f position, Vector3f forward, Vector3f up, Vector3f right) {
	public SnapshotComponent(short playerID, short tick, Vector3f position, Quaternionf orientation) {
		this.playerID = playerID;
		snapshots = new LinkedList();
		current = new Snapshot(tick, position, orientation);
//		current = new Snapshot(tick, position, forward, up, right);
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

//	public void add(short tick, Vector3f position, Vector3f forward, Vector3f up, Vector3f right) {
	public void add(short tick, Vector3f position, Quaternionf orientation) {
		Snapshot last = snapshots.peekLast();
		// Add snapshot if new tick is after latest tick or if new tick is in minimum bracket while latest tick is in maximum bracket
		if (isAfter(tick, next.getTick()) && (last == null || last != null && isAfter(tick, last.getTick()))) {
			snapshots.addLast(new Snapshot(tick, position, orientation));
//			snapshots.addLast(new Snapshot(tick, position, forward, up, right));
		}
	}

	public boolean isAfter(short newTick, short latestTick) {
		return (newTick > latestTick && !(newTick > 8000 && latestTick < 1000)) || (newTick < 1000 && latestTick > 8000);
	}

	public Snapshot getCurrent() {
		return current;
	}

	public Snapshot getNext() {
		return next;
	}

	public int getDifference() {
		if (current.getTick() > next.getTick() && !(current.getTick() > 8000 && next.getTick() < 1000) || next.getTick() > current.getTick() && !(next.getTick() > 8000 && current.getTick() < 1000))
			return next.getTick() - current.getTick();
		else
			return 0;
	}

	public Snapshot peekLatest() {
		return snapshots.peekLast();
	}

	public int size() {
		return snapshots.size();
	}
}
