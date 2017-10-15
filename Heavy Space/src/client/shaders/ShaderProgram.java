package client.shaders;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import utilities.FileUtils;

public abstract class ShaderProgram {
	private int programID;
	private int vertexShaderID;
	private int fragmentShaderID;

	private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

	public ShaderProgram(String vertexFile, String fragmentFile) {
		// Load shaders.
		vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
		// Create program for the shaders.
		programID = GL20.glCreateProgram();
		// Attach shaders to the program.
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		// Link and validate program.
		bindAttributes();
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		// Get all uniform locations specified for subclasses.
		getAllUniformLocations();
	}

	protected abstract void getAllUniformLocations();

	/**
	 * Create location (unique id) for variable name.
	 */
	protected int getUniformLocation(String uniformName) {
		return GL20.glGetUniformLocation(programID, uniformName);
	}

	protected void loadBoolean(int location, boolean value) {
		GL20.glUniform1f(location, value ? 1f : 0f);
	}

	protected void loadInt(int location, int value) {
		GL20.glUniform1i(location, value);
	}

	protected void loadFloat(int location, float value) {
		GL20.glUniform1f(location, value);
	}

	public void loadVector2f(int location, Vector2f vec2) {
		GL20.glUniform2f(location, vec2.x, vec2.y);
	}

	protected void loadVector3f(int location, Vector3f vec3) {
		GL20.glUniform3f(location, vec3.x, vec3.y, vec3.z);
	}

	protected void loadVector4f(int location, Vector4f vec4) {
		GL20.glUniform4f(location, vec4.x, vec4.y, vec4.z, vec4.w);
	}

	protected void loadMatrixf(int location, Matrix4f matrix) {
		glUniformMatrix4fv(location, false, matrix.get(matrixBuffer));
	}

	public void start() {
		GL20.glUseProgram(programID);
	}

	public void stop() {
		GL20.glUseProgram(0);
	}

	public void cleanUp() {
		stop();
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
	}

	protected abstract void bindAttributes();

	protected void bindAttribute(int attribute, String variableName) {
		GL20.glBindAttribLocation(programID, attribute, variableName);
	}

	private int loadShader(String file, int type) {
		// Convert file to string.
		String source = FileUtils.loadAsString(file);
		// Create unique id for the shader.
		int shaderID = glCreateShader(type);
		glShaderSource(shaderID, source);
		glCompileShader(shaderID);
		if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
			System.err.println("Failed to compile shader: " + file);
			System.err.println(glGetShaderInfoLog(shaderID));
			return -1;
		}
		return shaderID;
	}

}
