package shaders;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import entities.Camera;
import entities.Light;
import models.Model;

public class EntityShader extends ShaderProgram {
	private static final String VERTEX_FILE = "shaders/entity.vert";
	private static final String FRAGMENT_FILE = "shaders/entity.frag";

	public static final int INSTANCE_DATA_LENGTH = 48;
	public static final int MAX_LIGHTS = 64;

	private int location_mvp;
	private int location_modelView;
	private int location_model;

	private int location_view;
	private int location_projection;
	private int location_cameraPosition;
	// Lighting
	private int location_ambientLight;
	// Lighting lights
	private int location_numberOfLights;
	private int[] location_lightPosition;
	private int[] location_lightColor;
	private int[] location_attenuation;
	// Lighting material
	private int location_materialShininess;
	private int location_materialSpecularColor;
	// Textures
	private int location_atlasSize;
	private int location_textureOffset;
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
		// Lighting
		location_ambientLight = super.getUniformLocation("ambientLight");
		// Lighting lights
		location_numberOfLights = super.getUniformLocation("numberOfLights");
		location_lightPosition = new int[MAX_LIGHTS];
		location_lightColor = new int[MAX_LIGHTS];
		location_attenuation = new int[MAX_LIGHTS];
		for (int i = 0; i < MAX_LIGHTS; i++) {
			location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
			location_lightColor[i] = super.getUniformLocation("lightColor[" + i + "]");
			location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
		}
		// Lighting material
		location_materialShininess = super.getUniformLocation("materialShininess");
		location_materialSpecularColor = super.getUniformLocation("materialSpecularColor");

		// Textures
		location_atlasSize = super.getUniformLocation("atlasSize");
		location_textureOffset = super.getUniformLocation("textureOffset");
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
//		System.out.println("MVP");
//		System.out.println(mvp);
	}

	public void loadModelViewMatrix(Matrix4f modelView) {
		super.loadMatrixf(location_modelView, modelView);
//		System.out.println("modelView");
//		System.out.println(modelView);
	}

	public void loadModelMatrix(Matrix4f model) {
		super.loadMatrixf(location_model, model);
//		System.out.println("model");
//		System.out.println(model);
	}


	public void loadViewMatrix(Matrix4f view) {
		super.loadMatrixf(location_view, view);
//		System.out.println("view");
//		System.out.println(view);
	}

	public void loadProjectionMatrix(Matrix4f projection) {
		super.loadMatrixf(location_projection, projection);
//		System.out.println("projection");
//		System.out.println(projection);
	}

	public void loadCameraPosition(Camera camera) {
		super.loadVector3f(location_cameraPosition, camera.getPosition());
//		System.out.println("camera.getPosition()");
//		System.out.println(camera.getPosition().x + " " + camera.getPosition().y + " " + camera.getPosition().z);
	}

	public void loadAmbientLight(float ambientLight) {
		super.loadFloat(location_ambientLight, ambientLight);
//		System.out.println("ambientLight");
//		System.out.println(ambientLight);
	}

	public void loadLights(List<Light> lights) {
		int numberOfLights = lights.size() < MAX_LIGHTS ? lights.size() : MAX_LIGHTS;
		super.loadInt(location_numberOfLights, numberOfLights);
//		System.out.println("numberOfLights");
//		System.out.println(numberOfLights);
		for (int i = 0; i < MAX_LIGHTS && i < numberOfLights; i++) {
			super.loadVector3f(location_lightPosition[i], lights.get(i).getPosition());
			super.loadVector3f(location_lightColor[i], lights.get(i).getColor());
			super.loadVector3f(location_attenuation[i], lights.get(i).getAttenuation());
//			System.out.println("--- " + i );
//			System.out.println("lights.get(i).getPosition()");
//			System.out.println(lights.get(i).getPosition());
//			System.out.println("lights.get(i).getColor()");
//			System.out.println(lights.get(i).getColor());
//			System.out.println("lights.get(i).getAttenuation()");
//			System.out.println(lights.get(i).getAttenuation());
		}
	}
	
	
	
	
	

	
	
	
	
	

	public void loadSpecularLighting(Model model) {
		super.loadFloat(location_materialShininess, model.getShininess());
		super.loadVector3f(location_materialSpecularColor, model.getSpecularColor());
//		System.out.println("model.getShininess()");
//		System.out.println(model.getShininess());
//		System.out.println("model.getReflectivity()");
//		System.out.println(model.getReflectivity());
	}

	public void loadAtlasSize(int atlasSize) {
		super.loadInt(location_atlasSize, atlasSize);
//		System.out.println("atlasSize");
//		System.out.println(atlasSize);
	}

	public void loadTextureOffset(Vector2f textureOffset) {
		super.loadVector2f(location_textureOffset, textureOffset);
//		System.out.println("textureOffset");
//		System.out.println(textureOffset.x + " " + textureOffset.y);
	}

	public void loadAllowBackLighting(boolean allowBackLighting) {
		super.loadFloat(location_allowBackLighting, allowBackLighting ? 1 : 0);
//		System.out.println("allowBackLighting");
//		System.out.println(allowBackLighting);
	}
}
