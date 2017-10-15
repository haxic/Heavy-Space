package client.models;

public class Texture {
	private int textureID;
	private int atlasSize;
	private int texturePages;
	private float textureSize;

	public Texture(int textureID) {
		this.textureID = textureID;
		this.atlasSize = 1;
		this.texturePages = 1;
	}

	public Texture(int textureID, int atlasSize, int texturePages) {
		this.textureID = textureID;
		this.atlasSize = atlasSize;
		this.texturePages = texturePages;
		textureSize = atlasSize / texturePages;
	}

	public int getTextureID() {
		return textureID;
	}

	public int getAtlasSize() {
		return atlasSize;
	}

	public int getTexturePages() {
		return texturePages;
	}
	
	public float getTextureSize() {
		return textureSize;
	}
}
