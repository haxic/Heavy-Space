package client.renderers;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import client.entities.Camera;
import client.models.Model;
import client.shaders.SkyboxShader;

public class SkyboxRenderer {

	private SkyboxShader shader;

	public SkyboxRenderer() {
		shader = new SkyboxShader();
	}

	public void render(Camera camera, Model cube) {
		shader.start();
		shader.loadProjectionMatrix(camera.getProjectionMatrix());
		shader.loadViewMatrix(camera.getViewMatrix());
		// Bind VAO to use it.
		GL30.glBindVertexArray(cube.getMesh().getVaoID());
		// Bind VAO attribute list.
		GL20.glEnableVertexAttribArray(0);
		// Activate textures.
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, cube.getTexture().getTextureID());
		// Draw model.
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getMesh().getIndicesSize());
		// Unbind VAO attributes. NOT NEEDED?
		// GL20.glDisableVertexAttribArray(0);
		// Unbind VAO.
		GL30.glBindVertexArray(0);
		shader.stop();
	}

	public void cleanUp() {
		shader.cleanUp();
	}
}
