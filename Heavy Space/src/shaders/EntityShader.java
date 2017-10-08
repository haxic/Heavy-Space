package shaders;

import org.joml.Matrix4f;

import entities.Light;
import test.Camera;

public class EntityShader extends ShaderProgram {
	private static final String VERTEX_FILE = "shaders/entity.vert";
	private static final String FRAGMENT_FILE = "shaders/entity.frag";
	
	private int location_mvp;
	private int location_modelView;
	private int location_model;
	private int location_view;
	private int location_projection;
	private int location_cameraPosition;
	private int location_lightPosition;
	private int location_lightColor;
	private int location_allowBackLighting;
	
	public EntityShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_mvp = super.getUniformLocation("mvp");
		location_modelView = super.getUniformLocation("modelView");
		location_model = super.getUniformLocation("model");
		location_view = super.getUniformLocation("view");
		location_projection = super.getUniformLocation("projection");
		location_cameraPosition = super.getUniformLocation("cameraPosition");
		location_lightPosition = super.getUniformLocation("lightPosition");
		location_lightColor = super.getUniformLocation("lightColor");
		location_allowBackLighting = super.getUniformLocation("allowBackLighting");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "uv");
		super.bindAttribute(2, "normal");
	}

	public void loadModelViewProjectionMatrix(Matrix4f mvp) {
		super.loadMatrixf(location_mvp, mvp);
	}

	public void loadModelViewMatrix(Matrix4f modelView) {
		super.loadMatrixf(location_modelView, modelView);
	}

	public void loadModelMatrix(Matrix4f model) {
		super.loadMatrixf(location_model, model);
	}

	public void loadViewMatrix(Matrix4f view) {
		super.loadMatrixf(location_view, view);
	}

	public void loadProjectionMatrix(Matrix4f projection) {
		super.loadMatrixf(location_projection, projection);
	}
	
	public void loadCameraPosition(Camera camera) {
		super.loadVector3f(location_cameraPosition, camera.getPosition());
	}

	public void loadLight(Light light) {
		super.loadVector3f(location_lightPosition, light.getPosition());
		super.loadVector3f(location_lightColor, light.getColor());
	}

	public void loadAllowBackLighting(boolean allowBackLighting) {
		super.loadFloat(location_allowBackLighting, allowBackLighting ? 1 : 0);
	}


}
