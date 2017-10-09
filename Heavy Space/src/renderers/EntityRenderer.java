package renderers;

import java.util.List;
import java.util.Map;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import display.DisplayManager;
import entities.Actor;
import entities.Camera;
import entities.Entity;
import entities.Light;
import models.Mesh;
import models.Model;
import models.ModelAttachmentPoint;
import models.Texture;
import shaders.EntityShader;
import utilities.MatrixUtils;

public class EntityRenderer {
	EntityShader entityShader;

	public EntityRenderer() {
		entityShader = new EntityShader();
	}

	public void render(Camera camera, Light light, Map<Model, List<Actor>> actors) {
		prepareShader(camera, light);
		for (Model model : actors.keySet()) {
			prepareModel(model);
			List<Actor> batch = actors.get(model);
			for (Actor actor : batch) {
				prepareInstance(actor, camera);
				// Draw model.
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getMesh().getIndicesSize(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbindModel();
		}
		entityShader.stop();
	}

	private void prepareShader(Camera camera, Light light) {
		entityShader.start();
		entityShader.loadCameraPosition(camera);
		entityShader.loadProjectionMatrix(camera.getProjectionMatrix());
		entityShader.loadViewMatrix(camera.getViewMatrix());
		entityShader.loadLight(light);
	}

	private void prepareInstance(Actor actor, Camera camera) {
		Matrix4f projectionMatrix = camera.getProjectionMatrix();
		Matrix4f viewMatrix = camera.getViewMatrix();
		Entity entity = actor.getEntity();
		// TODO: Implement attachment placement.
		// Vector3f position = new Vector3f();
		// Vector3f rotation = new Vector3f();
		// if (actor.isAttached()) {
		// ModelAttachmentPoint modelAttachmentPoint = actor.getAttachementPoint();
		// Vector3f atp = actor.getModelAttachment().getAttachToActor().getEntity().getPosition();
		// Vector3f atr = actor.getModelAttachment().getAttachToActor().getEntity().getRotation();
		// Vector3f ats = actor.getModelAttachment().getAttachToActor().getEntity().getScale();
		// Vector3f ap = modelAttachmentPoint.getPosition();
		// Vector3f ar = modelAttachmentPoint.getRotation();
		// position.add(atp).add(ap);
		// rotation.add(atr);
		// } else {
		// position = entity.getPosition();
		// rotation = entity.getRotation();
		// }
		Matrix4f modelMatrix = MatrixUtils.createModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
		Matrix4f mvpMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f()).mul(modelMatrix);
		Matrix4f modelViewMatrix = viewMatrix.mul(modelMatrix, new Matrix4f());
		entityShader.loadModelViewProjectionMatrix(mvpMatrix);
		entityShader.loadModelViewMatrix(modelViewMatrix);
		entityShader.loadModelMatrix(modelMatrix);
		entityShader.loadTextureOffset(actor.getTextureOffset());
	}

	private void unbindModel() {
		RenderManager.enableBackCulling();
		// Unbind VAO attributes. NOT NEEDED?
		// GL20.glDisableVertexAttribArray(0);
		// GL20.glDisableVertexAttribArray(1);
		// GL20.glDisableVertexAttribArray(2);
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
		entityShader.loadAtlasSize(model.getTexture().getAtlasSize());
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
