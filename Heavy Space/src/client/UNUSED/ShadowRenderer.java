package client.UNUSED;

import java.util.List;
import java.util.Map;

import javax.swing.text.GlyphView;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import client.components.ActorComponent;
import client.components.LightComponent;
import client.display.DisplayManager;
import client.gameData.Camera;
import client.models.Mesh;
import client.models.Model;
import client.models.ModelAttachmentPoint;
import client.models.Texture;
import client.shaders.EntityShader;
import shared.game.Entity;
import utilities.Loader;
import utilities.MatrixUtils;

public class ShadowRenderer {
	ShadowShader shadowShader;
	private ShadowMap shadowMap;
	private Matrix4f mvp;

	public ShadowRenderer() {
		shadowShader = new ShadowShader();
		shadowMap = Loader.createShadowMap(1024, 1024);
	}

	public void render(Camera camera, List<LightComponent> lights, Map<Model, List<ActorComponent>> actors) {
		prepareShadowShader(camera, lights.get(0));
		for (Model model : actors.keySet()) {
			prepareModel(model);
			List<ActorComponent> batch = actors.get(model);
			for (ActorComponent actor : batch) {
				prepareInstance(actor, camera);
				// Draw model.
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getMesh().getIndicesSize(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbindModel();
		}
		shadowShader.stop();
	}

	private void prepareShadowShader(Camera camera, LightComponent light) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowMap.getFramebufferID());
		GL11.glViewport(0, 0, shadowMap.getWidth(), shadowMap.getHeight());
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		int shadowDistance = 100;
		int displayWidth = 1200;
		int displayHeight = 800;
		
		float farWidth = (float) (shadowDistance * Math.tan(Math.toRadians(camera.getFov())));
		float nearWidth = (float) (camera.getNear() * Math.tan(Math.toRadians(camera.getFov())));
		float farHeight = farWidth / displayWidth;
		float nearHeight = nearWidth / displayHeight;
		
		light.lightMatrix.setPerspective((float) Math.toRadians(45.0f), 1.0f, 0.1f, shadowDistance)
	     .lookAt(light.getLinearThrust(), new Vector3f(0, 0, 0), camera.getUp());
//		Matrix4f orthoProjMatrix = new Matrix4f().ortho(left, right, bottom, top, zNear, zFar)
		shadowShader.start();
	}

	private void prepareInstance(ActorComponent actor, Camera camera) {
		Matrix4f projectionMatrix = camera.getProjectionMatrix();
		Matrix4f viewMatrix = camera.getViewMatrix();
		Entity entity = actor.getEntity();
		Matrix4f modelMatrix = MatrixUtils.createModelMatrix(entity.getLinearThrust(), entity.getRotation(), entity.getScale());
		Matrix4f mvpMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f()).mul(modelMatrix);
		shadowShader.loadModelViewProjectionMatrix(mvpMatrix);
	}

	private void unbindModel() {
		GL30.glBindVertexArray(0);
	}

	private void prepareModel(Model model) {
		Mesh mesh = model.getMesh();
		// Bind VAO.
		GL30.glBindVertexArray(mesh.getVaoID());
		// Bind VAO attributes.
		GL20.glEnableVertexAttribArray(0);
	}

	public void cleanUp() {
		shadowShader.cleanUp();
	}
}
