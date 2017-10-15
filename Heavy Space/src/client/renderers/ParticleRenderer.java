package client.renderers;

import java.nio.FloatBuffer;
import java.util.List;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import client.entities.Camera;
import client.entities.Particle;
import client.models.Mesh;
import client.models.Texture;
import client.shaders.ParticleShader;
import utilities.Loader;

public class ParticleRenderer {

	private static final float[] VERTICES = { -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f };
	private static final int MAX_INSTANCES = 50000;
	private static final int INSTANCE_DATA_LENGTH = 21;

	private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(INSTANCE_DATA_LENGTH * MAX_INSTANCES);

	private Mesh quad;
	private Texture particleAtlasTexture;
	private ParticleShader shader;

	private Loader loader;
	private int vboID;
	private int pointer = 0;

	public ParticleRenderer(Loader loader, Texture particleAtlasTexture) {
		this.loader = loader;
		this.particleAtlasTexture = particleAtlasTexture;
		this.vboID = loader.createEmptyVBO(INSTANCE_DATA_LENGTH * MAX_INSTANCES);
		quad = loader.loadToVAO(VERTICES, 2);
		// Modelview matrix
		loader.addInstancedAttribute(quad.getVaoID(), vboID, 1, 4, INSTANCE_DATA_LENGTH, 0);
		loader.addInstancedAttribute(quad.getVaoID(), vboID, 2, 4, INSTANCE_DATA_LENGTH, 4);
		loader.addInstancedAttribute(quad.getVaoID(), vboID, 3, 4, INSTANCE_DATA_LENGTH, 8);
		loader.addInstancedAttribute(quad.getVaoID(), vboID, 4, 4, INSTANCE_DATA_LENGTH, 12);
		// Texture offsets
		loader.addInstancedAttribute(quad.getVaoID(), vboID, 5, 4, INSTANCE_DATA_LENGTH, 16);
		// Blend factors
		loader.addInstancedAttribute(quad.getVaoID(), vboID, 6, 1, INSTANCE_DATA_LENGTH, 20);

		shader = new ParticleShader();
	}

	public void render(List<Particle> particles, Camera camera, boolean renderSolidParticles) {
		prepare();
		shader.loadProjectionMatrix(camera.getProjectionMatrix());
		prepareTexture();
		if (renderSolidParticles)
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // Render latest on top. Use sorting on this.
		else
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // Additive blending. Good for fire etc, bad for solid particles.
		Matrix4f viewMatrix = camera.getViewMatrix();
		pointer = 0;
		float[] vboData = new float[particles.size() * INSTANCE_DATA_LENGTH];
		for (Particle particle : particles) {
			updateModelViewMatrix(particle, viewMatrix, vboData);
			updateTextureCoordinateData(particle, vboData);
		}
		loader.updateVBO(vboID, vboData, buffer);
		GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, quad.getIndicesSize(), particles.size());
		finishRendering();
	}

	private void prepareTexture() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, particleAtlasTexture.getTextureID());
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		shader.loadAtlasSize(particleAtlasTexture.getAtlasSize() * particleAtlasTexture.getTexturePages());
	}

	private static Matrix4f tempMatrix = new Matrix4f();

	private void updateModelViewMatrix(Particle particle, Matrix4f viewMatrix, float[] vboData) {
		Matrix4f modelMatrix = tempMatrix.identity();
		modelMatrix.translate(particle.getPosition());
		modelMatrix.m00(viewMatrix.m00());
		modelMatrix.m01(viewMatrix.m10());
		modelMatrix.m02(viewMatrix.m20());
		modelMatrix.m10(viewMatrix.m01());
		modelMatrix.m11(viewMatrix.m11());
		modelMatrix.m12(viewMatrix.m21());
		modelMatrix.m20(viewMatrix.m02());
		modelMatrix.m21(viewMatrix.m12());
		modelMatrix.m22(viewMatrix.m22());
		Matrix4f modelViewMatrix = viewMatrix.mul(modelMatrix, tempMatrix);
		modelViewMatrix.rotateZ((float) Math.toRadians(particle.getRotation()));
		modelViewMatrix.scale(particle.getScale());
		storeMatrixData(modelViewMatrix, vboData);
	}

	private void storeMatrixData(Matrix4f matrix, float[] vboData) {
		vboData[pointer++] = matrix.m00();
		vboData[pointer++] = matrix.m01();
		vboData[pointer++] = matrix.m02();
		vboData[pointer++] = matrix.m03();
		vboData[pointer++] = matrix.m10();
		vboData[pointer++] = matrix.m11();
		vboData[pointer++] = matrix.m12();
		vboData[pointer++] = matrix.m13();
		vboData[pointer++] = matrix.m20();
		vboData[pointer++] = matrix.m21();
		vboData[pointer++] = matrix.m22();
		vboData[pointer++] = matrix.m23();
		vboData[pointer++] = matrix.m30();
		vboData[pointer++] = matrix.m31();
		vboData[pointer++] = matrix.m32();
		vboData[pointer++] = matrix.m33();
	}

	private void updateTextureCoordinateData(Particle particle, float[] data) {
		data[pointer++] = particle.getTextureOffset1().x;
		data[pointer++] = particle.getTextureOffset1().y;
		data[pointer++] = particle.getTextureOffset2().x;
		data[pointer++] = particle.getTextureOffset2().y;
		data[pointer++] = particle.getBlendFactor();
	}

	public void cleanUp() {
		shader.cleanUp();
	}

	private void prepare() {
		shader.start();
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		GL20.glEnableVertexAttribArray(5);
		GL20.glEnableVertexAttribArray(6);
		// Enable transparency.
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthMask(false);
	}

	private void finishRendering() {
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL20.glDisableVertexAttribArray(5);
		GL20.glDisableVertexAttribArray(6);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

}
