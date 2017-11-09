package shared.components;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import hecs.EntityComponent;

public class UnitComponent implements EntityComponent {
	private Vector3f position;
	private Vector3f rotation;
	private Vector3f scale;
	private int id;

	private Vector3f forward;
	private Vector3f up;
	private Vector3f right;

	public UnitComponent(Vector3f position, Vector3f rotation, Vector3f scale) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		forward = new Vector3f(0, 0, -1);
		right = new Vector3f(1, 0, 0);
		up = new Vector3f(0, 1, 0);
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}


	public Vector3f getRotation() {
		return rotation;
	}

	public void setRotation(Vector3f rotation) {
		this.rotation = rotation;
	}

	private static Vector3f tempVector1 = new Vector3f();
	private static Vector3f tempVector2 = new Vector3f();

	public void pitch(double angle) {
		// D = normalize(D * cos a + up * sin a)
		forward.mul((float) Math.cos(angle), tempVector1).add(up.mul((float) Math.sin(angle), tempVector2)).normalize(forward);
		// up = cross(R, direction);
		right.cross(forward, up);
	}

	public void roll(double angle) {
		// right = normalize(right * cos a + up * sin a)
		right.mul((float) Math.cos(angle), tempVector1).add(up.mul((float) Math.sin(angle), tempVector2)).normalize(right);
		// up = cross(right, direction);
		right.cross(forward, up);
	}

	public void yaw(double angle) {
		// right = right * cos a + direction * sin a
		right.mul((float) Math.cos(angle), tempVector1).add(forward.mul((float) Math.sin(angle), tempVector2), right);
		// direction = cross(up, right)
		up.cross(right, forward);
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

	public Vector3f getScale() {
		return scale;
	}

	public void setScale(Vector3f scale) {
		this.scale = scale;
	}

	public Matrix4f getRotationMatrix() {
		Matrix4f matrix = new Matrix4f(right.x, right.y, right.z, 0, forward.x, forward.y, forward.z, 0, up.x, up.y, up.z, 0, 0, 0, 0, 1);
		return matrix;
	}
}
