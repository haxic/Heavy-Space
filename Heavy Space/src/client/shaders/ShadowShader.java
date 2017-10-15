package client.shaders;

import org.joml.Matrix4f;

public class ShadowShader extends ShaderProgram {
	private static final String VERTEX_FILE = "shaders/shadow.vert";
	private static final String FRAGMENT_FILE = "shaders/shadow.frag";
	private int location_mvp;

	public ShadowShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);

	}

	@Override
	protected void getAllUniformLocations() {
		location_mvp = super.getUniformLocation("mvp");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

	public void loadModelViewProjectionMatrix(Matrix4f mvp) {
		super.loadMatrixf(location_mvp, mvp);
	}
}
