package utilities;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MatrixUtils {
	private static Matrix4f tempMatrix = new Matrix4f();

	public static Matrix4f createModelMatrix(Vector3f position, Vector3f rotation, Vector3f scale) {
		tempMatrix.identity();
		tempMatrix.translate(position);
		tempMatrix.rotateX((float) Math.toRadians(rotation.x));
		tempMatrix.rotateY((float) Math.toRadians(rotation.y));
		tempMatrix.rotateZ((float) Math.toRadians(rotation.z));
		tempMatrix.scale(scale);
		return tempMatrix;
	}

	public static Matrix4f createViewMatrix(Vector3f position, Vector3f rotation, Vector3f up) {
		tempMatrix.identity().lookAt(position, rotation, up);
		return tempMatrix;
	}

	public static Matrix4f createProjectionMatrix(float fov, float aspect, float near, float far) {
		tempMatrix.identity().perspective(fov, aspect, near, far);
		return tempMatrix;
	}
}
