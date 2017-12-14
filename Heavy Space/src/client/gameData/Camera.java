package client.gameData;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	public Vector3f position;
	public Vector3f forward;
	public Vector3f up;
	public Vector3f right;
	float fov, aspectRatio, near, far;
	boolean lockUp;

	Matrix4f projectionMatrix = new Matrix4f();
	Matrix4f viewMatrix = new Matrix4f();

	private static Vector3f tempVector1 = new Vector3f();
	private static Vector3f tempVector2 = new Vector3f();

	public Camera() {
		position = new Vector3f(0, 0, 0);
		forward = new Vector3f(0, 0, -1);
		right = new Vector3f(1, 0, 0);
		up = new Vector3f(0, 1, 0);
		fov = 70;
		near = 1;
		far = 20000;
	}

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

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getForward() {
		return forward;
	}

	public void setForward(Vector3f forward) {
		this.forward = forward;
	}

	public Vector3f getUp() {
		return up;
	}

	public void setUp(Vector3f up) {
		this.up = up;
	}
	
	public Vector3f getRight() {
		return right;
	}

	public float getFov() {
		return fov;
	}

	public void setFov(float fov) {
		this.fov = fov;
	}

	public float getNear() {
		return near;
	}

	public void setNear(float near) {
		this.near = near;
	}

	public float getFar() {
		return far;
	}

	public void setFar(float far) {
		this.far = far;
	}

	public void setProjectionMatrix(float fov, float aspectRatio, float near, float far) {
		this.fov = fov;
		this.aspectRatio = aspectRatio;
		this.near = near;
		this.far = far;
		projectionMatrix.identity().perspective((float) Math.toRadians(getFov()), aspectRatio, getNear(), getFar());
	}

	public void updateProjectionMatrix(float aspectRatio) {
		if (this.aspectRatio == aspectRatio)
			return;
		this.aspectRatio = aspectRatio;
		projectionMatrix.identity().perspective((float) Math.toRadians(getFov()), aspectRatio, getNear(), getFar());
	}

	public void updateViewMatrix() {
		viewMatrix.identity().lookAt(getPosition(), getPosition().add(getForward(), tempVector1), getUp());
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public void toggleLockUp() {
		lockUp = !lockUp;
	}

	public float getAspectRatio() {
		return aspectRatio;
	}
}
