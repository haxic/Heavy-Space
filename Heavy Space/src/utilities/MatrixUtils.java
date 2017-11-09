package utilities;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import shared.components.UnitComponent;

public class MatrixUtils {
	private static Matrix4f tempMatrix1 = new Matrix4f();
	private static Vector3f temp1 = new Vector3f();

	public static Matrix4f createModelMatrix(Vector3f position, Vector3f rotation, Vector3f scale) {
		tempMatrix1.identity();
		tempMatrix1.translate(position);
		tempMatrix1.rotateX((float) Math.toRadians(rotation.x));
		tempMatrix1.rotateY((float) Math.toRadians(rotation.y));
		tempMatrix1.rotateZ((float) Math.toRadians(rotation.z));
		tempMatrix1.scale(scale);
		return tempMatrix1;
	}

	public static Matrix4f createModelMatrix(UnitComponent unitComponent) {
		tempMatrix1.identity();
		tempMatrix1.translate(unitComponent.getPosition());
		tempMatrix1.mul(new Matrix4f(unitComponent.getRight().x, unitComponent.getRight().y, unitComponent.getRight().z, 0, unitComponent.getForward().x, unitComponent.getForward().y,
				unitComponent.getForward().z, 0, unitComponent.getUp().x, unitComponent.getUp().y, unitComponent.getUp().z, 0, 0, 0, 0, 1));
		tempMatrix1.scale(unitComponent.getScale());
		return tempMatrix1;
	}

	public static Matrix4f createModelMatrix(Vector3f position, Matrix4f rotationMatrix, Vector3f scale) {
		tempMatrix1.identity();
		tempMatrix1.translate(position);
		tempMatrix1.mul(rotationMatrix);
		tempMatrix1.scale(scale);
		return tempMatrix1;
	}

	public static Matrix4f createViewMatrix(Vector3f position, Vector3f direction, Vector3f up) {
		tempMatrix1.identity().lookAt(position, direction, up);
		return tempMatrix1;
	}

	public static Matrix4f createViewMatrix(Quaternionf rotation) {
		tempMatrix1.set(rotation);
		return tempMatrix1;
	}

	public static Matrix4f createProjectionMatrix(float fov, float aspect, float near, float far) {
		tempMatrix1.identity().perspective(fov, aspect, near, far);
		return tempMatrix1;
	}

}
