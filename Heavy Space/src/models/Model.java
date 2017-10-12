package models;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import gameData.ModelAttachementTag;

public class Model {
	private Mesh mesh;
	private Texture texture;
	private boolean hasTransparency;
	private boolean allowBackLighting;

	private Map<ModelAttachementTag, ModelAttachmentPoint> modelAttachmentPoints = new HashMap<ModelAttachementTag, ModelAttachmentPoint>();
	private float materialShininess = 5f;
	private Vector3f materialSpecularColor = new Vector3f(0.5f, 0.5f, 0.5f);

	public Model(Mesh mesh, Texture texture) {
		this.mesh = mesh;
		this.texture = texture;
	}

	public Mesh getMesh() {
		return mesh;
	}

	public Texture getTexture() {
		return texture;
	}

	public boolean hasTransparency() {
		return hasTransparency;
	}

	public boolean isBackLightingAllowed() {
		return allowBackLighting;
	}

	public void setHasTransparency(boolean hasTransparency) {
		this.hasTransparency = hasTransparency;
	}

	public void setAllowBackLighting(boolean allowBackLighting) {
		this.allowBackLighting = allowBackLighting;
	}

	public ModelAttachmentPoint getAttachementPoint(ModelAttachementTag modelAttachementTag) {
		return modelAttachmentPoints.get(modelAttachementTag);
	}

	public void putAttachmentPoint(ModelAttachementTag tag, ModelAttachmentPoint modelAttachmentPoint) {
		modelAttachmentPoints.put(tag, modelAttachmentPoint);
	}

	public float getShininess() {
		return materialShininess;
	}

	public Vector3f getSpecularColor() {
		return materialSpecularColor;
	}
}
