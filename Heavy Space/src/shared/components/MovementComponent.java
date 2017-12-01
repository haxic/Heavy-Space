package shared.components;

import org.joml.Vector3f;

import hecs.EntityComponent;

public class MovementComponent extends EntityComponent {
	private Vector3f linearAcc = new Vector3f();
	private Vector3f linearVel = new Vector3f();
	private Vector3f angularAcc = new Vector3f();
	private Vector3f angularVel = new Vector3f();

	public Vector3f getLinearAcc(Vector3f dest) {
		return dest.set(linearAcc);
	}

	public Vector3f getLinearAcc() {
		return linearAcc;
	}

	public Vector3f getLinearVel(Vector3f dest) {
		return dest.set(linearVel);
	}

	public Vector3f getLinearVel() {
		return linearVel;
	}

	public Vector3f getAngularAcc(Vector3f dest) {
		return dest.set(angularAcc);
	}

	public Vector3f getAngularAcc() {
		return angularAcc;
	}

	public Vector3f getAngularVel(Vector3f dest) {
		return dest.set(angularVel);
	}

	public Vector3f getAngularVel() {
		return angularVel;
	}

	@Override
	protected void removeComponent() {
	}

}
