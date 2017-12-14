package client.gameData;

import org.joml.Vector3f;

public class Snapshot {
	private short tick;
	private Vector3f position;
	private Vector3f forward;
	private Vector3f up;
	private Vector3f right;

	public Snapshot(short tick, Vector3f position, Vector3f forward, Vector3f up, Vector3f right) {
		this.tick = tick;
		this.position = position;
		this.forward = forward;
		this.up = up;
		this.right = right;
	}

	public short getTick() {
		return tick;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getForward() {
		return forward;
	}
	
	public Vector3f getUp() {
		return up;
	}
	
	public Vector3f getRight() {
		return right;
	}
}
