package shaders;

import org.joml.Matrix4f;
import org.joml.Vector2f;


public class ParticleShader extends ShaderProgram {

	private static final String VERTEX_FILE = "shaders/particle.vert";
	private static final String FRAGMENT_FILE = "shaders/particle.frag";

	private int location_atlasSize;
	private int location_projectionMatrix;

	public ParticleShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_atlasSize = super.getUniformLocation("atlasSize");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "modelViewMatrix");
		super.bindAttribute(5, "texOffsets");
		super.bindAttribute(6, "blendFactor");
	}

	Vector2f tempVector = new Vector2f();

	public void loadAtlasSize(float atlasSize) {
		super.loadFloat(location_atlasSize, atlasSize);
	}

	public void loadProjectionMatrix(Matrix4f projection) {
		super.loadMatrixf(location_projectionMatrix, projection);
	}

}
