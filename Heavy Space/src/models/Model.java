package models;

public class Model {
	private Mesh mesh;
	private Texture texture;
	private boolean hasTransparency;
	private boolean allowBackLighting;

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
}
