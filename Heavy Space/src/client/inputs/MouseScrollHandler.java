package client.inputs;

import org.lwjgl.glfw.GLFWScrollCallbackI;

public class MouseScrollHandler implements GLFWScrollCallbackI {
	private static float xoffset;
	private static float yoffset;
	private static boolean xChanged;
	private static boolean yChanged;

	public void invoke(long window, double xoffset, double yoffset) {
		MouseScrollHandler.xoffset = (float) xoffset;
		MouseScrollHandler.yoffset = (float) yoffset;
		if (xoffset != 0)
			xChanged = true;
		if (yoffset != 0)
			yChanged = true;
	}

	public synchronized static void poll() {
		if (!xChanged)
			xoffset = 0;
		if (!yChanged)
			yoffset = 0;
		xChanged = false;
		yChanged = false;
	}

	public static float getXOffset() {
		return xoffset;
	}

	public static float getYOffset() {
		return yoffset;
	}
}
