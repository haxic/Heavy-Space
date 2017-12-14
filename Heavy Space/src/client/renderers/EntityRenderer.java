package client.renderers;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import client.components.ActorComponent;
import client.components.LightComponent;
import client.gameData.Camera;
import client.models.Mesh;
import client.models.Model;
import client.models.Texture;
import client.shaders.EntityShader;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.ObjectComponent;
import utilities.MatrixUtils;

public class EntityRenderer {
	EntityShader entityShader;

	public EntityRenderer() {
		entityShader = new EntityShader();
	}

	public void render(EntityManager entityManager, Camera camera, List<Entity> lights, Map<Model, List<Entity>> entities) {
		prepareEntityShader(entityManager, camera, lights);
		for (Model model : entities.keySet()) {
			prepareModel(model);
			List<Entity> batch = entities.get(model);
			for (Entity entity : batch) {
				prepareInstance(entityManager, entity, camera);
				// Draw model.
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getMesh().getIndicesSize(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbindModel();
		}
		entityShader.stop();
	}

	private void prepareEntityShader(EntityManager entityManager, Camera camera, List<Entity> lights) {
		entityShader.start();
		entityShader.loadCameraPosition(camera);
		entityShader.loadProjectionMatrix(camera.getProjectionMatrix());
		entityShader.loadViewMatrix(camera.getViewMatrix());
		entityShader.loadAmbientLight(0);
		entityShader.loadLights(entityManager, lights);
	}

	private void prepareInstance(EntityManager entityManager, Entity entity, Camera camera) {
		Matrix4f projectionMatrix = camera.getProjectionMatrix();
		Matrix4f viewMatrix = camera.getViewMatrix();
		ActorComponent actorComponent = (ActorComponent) entityManager.getComponentInEntity(entity, ActorComponent.class);
		ObjectComponent unitComponent = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
		// TODO: Implement attachment placement.
		// Vector3f position = new Vector3f();
		// Vector3f rotation = new Vector3f();
		// if (actor.isAttached()) {
		// ModelAttachmentPoint modelAttachmentPoint =
		// actor.getAttachementPoint();
		// Vector3f atp =
		// actor.getModelAttachment().getAttachToActor().getEntity().getPosition();
		// Vector3f atr =
		// actor.getModelAttachment().getAttachToActor().getEntity().getRotation();
		// Vector3f ats =
		// actor.getModelAttachment().getAttachToActor().getEntity().getScale();
		// Vector3f ap = modelAttachmentPoint.getPosition();
		// Vector3f ar = modelAttachmentPoint.getRotation();
		// position.add(atp).add(ap);
		// rotation.add(atr);
		// } else {
		// position = entity.getPosition();
		// rotation = entity.getRotation();
		// }
//		Matrix4f modelMatrix = MatrixUtils.createModelMatrix(unitComponent.getPosition(), unitComponent.getRotationMatrix(), unitComponent.getScale());
		Matrix4f modelMatrix = MatrixUtils.createModelMatrix(unitComponent);
		Matrix4f mvpMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f()).mul(modelMatrix);
		Matrix4f modelViewMatrix = viewMatrix.mul(modelMatrix, new Matrix4f());
		entityShader.loadModelViewProjectionMatrix(mvpMatrix);
		entityShader.loadModelViewMatrix(modelViewMatrix);
		entityShader.loadModelMatrix(modelMatrix);
		entityShader.loadTextureOffset(actorComponent.getTextureOffset());
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

		// Enable back culling for entities that has transparency.
		if (model.hasTransparency())
			RenderManager.disableBackCulling();
		// Enable lighting for back side of polygons (opposite side of normals)
		entityShader.loadAllowBackLighting(model.isBackLightingAllowed());
		entityShader.loadSpecularLighting(model);
		// Bind textures.
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
		entityShader.loadAtlasSize(model.getTexture().getAtlasSize());
	}

	public void cleanUp() {
		entityShader.cleanUp();
	}
}
