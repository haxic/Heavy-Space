package client.models;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class ShadowMap {


	private int framebufferID;
	private int textureID;
	private int width;
	private int height;

	public ShadowMap(int framebufferID, int textureID, int width, int height) {
		this.framebufferID = framebufferID;
		this.textureID = textureID;
		this.width = width;
		this.height = height;
	}

	public void cleanUp() {
		GL30.glDeleteFramebuffers(framebufferID);
		GL11.glDeleteTextures(textureID);
	}
	
	public int getFramebufferID() {
		return framebufferID;
	}

	public int getTextureID() {
		return textureID;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
