package client.components;

import org.joml.Vector3f;

public class Snapshot {
	private short tick;
	private Vector3f position;
	private Vector3f forward;
	private Vector3f up;

	public Snapshot(short tick, Vector3f position, Vector3f forward, Vector3f up) {
		this.tick = tick;
		this.position = position;
		this.forward = forward;
		this.up = up;
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
}
