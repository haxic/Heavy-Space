package shaders;

import org.joml.Matrix4f;

public class SkyboxShader extends ShaderProgram {

	private static final String VERTEX_FILE = "shaders/skybox.vert";
	private static final String FRAGMENT_FILE = "shaders/skybox.frag";

	private int location_projectionMatrix;
	private int location_viewMatrix;

	public SkyboxShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

	public void loadProjectionMatrix(Matrix4f matrix) {
		super.loadMatrixf(location_projectionMatrix, matrix);
	}

	Matrix4f tempMatrix = new Matrix4f();
	public void loadViewMatrix(Matrix4f matrix) {
		matrix = tempMatrix.set(matrix);
		matrix.m30(0);
		matrix.m31(0);
		matrix.m32(0);
		super.loadMatrixf(location_viewMatrix, matrix);
	}
}
