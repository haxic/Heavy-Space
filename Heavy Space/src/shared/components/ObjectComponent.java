package shared.components;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import hecs.EntityComponent;

public class ObjectComponent extends EntityComponent {
	private Vector3f position;
	private Vector3f scale;

	private Vector3f forward;
	private Vector3f up;
	private Vector3f right;

	public ObjectComponent(Vector3f position, Vector3f scale) {
		this.position = position;
		this.scale = scale;
		forward = new Vector3f(0, 0, -1);
		right = new Vector3f(1, 0, 0);
		up = new Vector3f(0, 1, 0);
	}

	public ObjectComponent(Vector3f position) {
		this.position = position;
		scale = new Vector3f(1, 1, 1);
		forward = new Vector3f(0, 0, -1);
		right = new Vector3f(1, 0, 0);
		up = new Vector3f(0, 1, 0);
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

	public Vector3f getPosition(Vector3f dest) {
		return dest.set(position);
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getForward(Vector3f dest) {
		return dest.set(forward);
	}

	public Vector3f getForward() {
		return forward;
	}

	public Vector3f getUp(Vector3f dest) {
		return dest.set(up);
	}

	public Vector3f getUp() {
		return up;
	}

	public Vector3f getRight(Vector3f dest) {
		return dest.set(right);
	}

	public Vector3f getRight() {
		return right;
	}

	public Vector3f getScale(Vector3f dest) {
		return dest.set(scale);
	}

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
}
