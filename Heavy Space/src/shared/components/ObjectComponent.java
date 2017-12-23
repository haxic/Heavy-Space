package shared.components;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import hecs.EntityComponent;

public class ObjectComponent extends EntityComponent {
	private static final Vector3f FORWARD = new Vector3f(0, 0, -1);
	private static final Vector3f UP = new Vector3f(0, 1, 0);
	private static final Vector3f RIGHT = new Vector3f(1, 0, 0);
	private Vector3f position;
	private Vector3f scale;

	private Vector3f forward;
	private Vector3f up;
	private Vector3f right;

	private Quaternionf orientation;

	public ObjectComponent(Vector3f position, Vector3f scale) {
		this.position = position;
		this.scale = scale;
		orientation = new Quaternionf();
		forward = new Vector3f(FORWARD);
		right = new Vector3f(RIGHT);
		up = new Vector3f(UP);

	}

	public ObjectComponent(Vector3f position) {
		this.position = position;
		orientation = new Quaternionf();
		scale = new Vector3f(1, 1, 1);
		forward = new Vector3f(FORWARD);
		right = new Vector3f(RIGHT);
		up = new Vector3f(UP);
	}

	public void rotate(float pitch, float yaw, float roll) {
		orientation.rotate(pitch, yaw, roll);
		forward.set(FORWARD).rotate(orientation);
		right.set(RIGHT).rotate(orientation);
		up.set(UP).rotate(orientation);
	}
	
	public void updateOrientation() {
		forward.set(FORWARD).rotate(orientation);
		right.set(RIGHT).rotate(orientation);
		up.set(UP).rotate(orientation);
	}

	// public Vector3f getPosition(Vector3f dest) {
	// return dest.set(position);
	// }

	public Vector3f getPosition() {
		return position;
	}

	// public Vector3f getForward(Vector3f dest) {
	// return dest.set(forward);
	// }

	public Vector3f getForward() {
		return forward;
	}

	// public Vector3f getUp(Vector3f dest) {
	// return dest.set(up);
	// }

	public Vector3f getUp() {
		return up;
	}

	// public Vector3f getRight(Vector3f dest) {
	// return dest.set(right);
	// }

	public Vector3f getRight() {
		return right;
	}

	// public Vector3f getScale(Vector3f dest) {
	// return dest.set(scale);
	// }

	public Vector3f getScale() {
		return scale;
	}

	public Matrix4f getRotationMatrix(Matrix4f dest) {
		dest.set(right.x, right.y, right.z, 0, forward.x, forward.y, forward.z, 0, up.x, up.y, up.z, 0, 0, 0, 0, 1);
		return dest;
	}

	@Override
	protected void removeComponent() {
	}

	public Quaternionf getOrientation() {
		return orientation;
	}
}
