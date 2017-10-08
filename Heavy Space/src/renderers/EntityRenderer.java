package renderers;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import display.DisplayManager;
import entities.Actor;
import entities.Entity;
import entities.Light;
import models.Mesh;
import models.Model;
import models.Texture;
import shaders.EntityShader;
import test.Camera;
import utilities.MatrixUtils;

public class EntityRenderer {
	EntityShader entityShader;

	public EntityRenderer(DisplayManager displayManager) {
		entityShader = new EntityShader();
	}

	public void render(Camera camera, Light light, Map<Model, List<Actor>> actors) {
		entityShader.start();
		entityShader.loadCameraPosition(camera);
		entityShader.loadProjectionMatrix(camera.getProjectionMatrix());
		entityShader.loadViewMatrix(camera.getViewMatrix());
		entityShader.loadLight(light);
		for (Model model : actors.keySet()) {
			prepareModel(model);
			List<Actor> batch = actors.get(model);
			for (Actor actor : batch) {
				prepareInstance(actor, camera);
				// Draw model.
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getMesh().getIndicesSize(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbindModel(model);
		}
		entityShader.stop();
	}

	private void prepareInstance(Actor actor, Camera camera) {
		Matrix4f projectionMatrix = camera.getProjectionMatrix();
		Matrix4f viewMatrix = camera.getViewMatrix();
		Entity entity = actor.getEntity();
		Matrix4f modelMatrix = MatrixUtils.createModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
		Matrix4f mvpMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f()).mul(modelMatrix);
		Matrix4f modelViewMatrix = viewMatrix.mul(modelMatrix, new Matrix4f());
		entityShader.loadModelViewProjectionMatrix(mvpMatrix);
		entityShader.loadModelViewMatrix(modelViewMatrix);
		entityShader.loadModelMatrix(modelMatrix);
	}

	private void unbindModel(Model model) {
		// MasterRenderer.enableBackCulling();
		Texture texture = model.getTexture();
		// Unbind texture.
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
		// Unbind VAO attributes.
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		// Unbind VAO.
		GL30.glBindVertexArray(0);
	}

	private void prepareModel(Model model) {
		Texture texture = model.getTexture();
		Mesh mesh = model.getMesh();
		// Bind VAO.
		GL30.glBindVertexArray(mesh.getVaoID());
		// Bind VAO attributes.
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		// Bind textures.
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());

		// Enable back culling for entities that has transparency.
		if (model.hasTransparency())
			RenderManager.disableBackCulling();
		// Enable lighting for back side of polygons (opposite side of normals)
		entityShader.loadAllowBackLighting(model.isBackLightingAllowed());
	}

	public void cleanUp() {
		entityShader.cleanUp();
	}
}
