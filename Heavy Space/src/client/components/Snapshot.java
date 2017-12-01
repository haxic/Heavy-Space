package client.components;

import org.joml.Vector3f;

public class Snapshot {
	short tick;
	Vector3f position;

	public Snapshot(short tick, Vector3f position) {
		this.tick = tick;
		this.position = position;
	}

	public short getTick() {
		return tick;
	}
	
	public Vector3f getPosition() {
		return position;
	}

}
