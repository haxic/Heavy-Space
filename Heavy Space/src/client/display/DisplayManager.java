package client.display;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

import client.inputs.KeyboardHandler;
import client.inputs.MouseHandler;
import client.inputs.MouseScrollHandler;

public class DisplayManager {
	private boolean cursorEnabled = true;
	private long windowID;
	private int width, height;
	private double aspect;
	private double lastTime;

	private float deltaTime;
	private float mouseX;
	private float mouseY;

	public DisplayManager(int width, int height) {
		if (!GLFW.glfwInit()) {
			System.err.println("Could not initialize GLFW!");
			return;
		}
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);

		windowID = GLFW.glfwCreateWindow(width, height, "Heavy Space", NULL, NULL);
		if (windowID == NULL) {
			System.err.println("Could not create GLFW window!");
			return;
		}
		GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		GLFW.glfwSetWindowPos(windowID, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2);
		GLFW.glfwMakeContextCurrent(windowID);

		GLFW.glfwSetScrollCallback(windowID, new MouseScrollHandler());
		GLFW.glfwSetMouseButtonCallback(windowID, new MouseHandler());
		GLFW.glfwSetKeyCallback(windowID, new KeyboardHandler());

		GLFW.glfwShowWindow(windowID);
		GL.createCapabilities();
		GLCapabilities caps = GL.getCapabilities();
		if (caps.OpenGL30) {
			System.out.println("Use GL30");
		} else if (caps.GL_ARB_framebuffer_object) {
			System.out.println("Use ARBFramebufferObject");
		} else if (caps.GL_EXT_framebuffer_object) {
			System.out.println("Use EXTFramebufferObject");
		} else
			throw new UnsupportedOperationException();
		this.width = width;
		this.height = height;
		aspect = (double) width / (double) height;
		GL11.glViewport(0, 0, width, height);
		GLFW.glfwSwapInterval(1);
		// GLFW.glfwSetInputMode(windowID, GLFW.GLFW_STICKY_KEYS, GL11.GL_TRUE);
		// setBasicAntialising(4);
	}

	public void handleInputs() {
		double currentTime = GLFW.glfwGetTime();
		deltaTime = (float) (GLFW.glfwGetTime() - lastTime);
		lastTime = currentTime;
		DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
		GLFW.glfwGetCursorPos(windowID, x, y);
		x.rewind();
		y.rewind();
		mouseX = (float) x.get();
		mouseY = (float) y.get();
		if (!cursorEnabled)
			GLFW.glfwSetCursorPos(windowID, width / 2, height / 2);
		KeyboardHandler.poll();
		MouseHandler.poll();
		MouseScrollHandler.poll();
	}

	public void updateDisplay() {
		GLFW.glfwPollEvents();
		updateWindowDimension();
		GLFW.glfwSwapBuffers(windowID);
	}

	private void updateWindowDimension() {
		IntBuffer w = BufferUtils.createIntBuffer(4);
		IntBuffer h = BufferUtils.createIntBuffer(4);
		GLFW.glfwGetWindowSize(windowID, w, h);
		int width = w.get(0);
		int height = h.get(0);
		if (this.width == width && this.height == height)
			return;
		this.width = width;
		this.height = height;
		aspect = (double) this.width / (double) this.height;
		GL11.glViewport(0, 0, this.width, this.height);
	}

	public float getDeltaTime() {
		return deltaTime;
	}

	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(windowID);
	}

	public void toggleCursor() {
		if (cursorEnabled)
			disableCursor();
		else
			enableCursor();
	}

	public void enableCursor() {
		cursorEnabled = true;
		glfwSetInputMode(windowID, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		// TODO: Don't use syso!
		System.out.println("ENABLE CURSOR " + cursorEnabled);
	}

	public void disableCursor() {
		cursorEnabled = false;
		glfwSetInputMode(windowID, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		glfwSetCursorPos(windowID, getWidth() / 2, getHeight() / 2);
		// TODO: Don't use syso!
		System.out.println("DISABLE CURSOR " + cursorEnabled);
	}

	public void closeDisplay() {
		GLFW.glfwDestroyWindow(windowID);
		GLFW.glfwTerminate();
	}

	public void setBasicAntialising(int value) {
		GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, value);
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public float getAspectRatio() {
		return (float) aspect;
	}

	public float getMouseX() {
		return mouseX;
	}

	public float getMouseY() {
		return mouseY;
	}

	public boolean keyPressed(int key) {
		return GLFW.glfwGetKey(windowID, key) == GLFW.GLFW_PRESS;
	}

	public boolean isCursorEnabled() {
		return cursorEnabled;
	}

}
