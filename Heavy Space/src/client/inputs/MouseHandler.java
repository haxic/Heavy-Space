package client.inputs;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

public class MouseHandler extends GLFWMouseButtonCallback {

	private static final int BUTTON_COUNT = 65536;
	public static boolean[] currentClicks = new boolean[BUTTON_COUNT];
	private static KeyState[] clicks = new KeyState[BUTTON_COUNT];

	public void invoke(long window, int button, int action, int mods) {
		if (button >= 0 && button < BUTTON_COUNT) {
			currentClicks[button] = action != GLFW.GLFW_RELEASE;
		}
	}

	private enum KeyState {
		RELEASED, // Not down
		PRESSED, // Down, but not the first time
		ONCE // Down for the first time
	}

	public synchronized static void poll() {
		for (int i = 0; i < BUTTON_COUNT; ++i) {
			if (currentClicks[i]) {
				if (clicks[i] == KeyState.RELEASED) {
					clicks[i] = KeyState.ONCE;
				} else
					clicks[i] = KeyState.PRESSED;
			} else {
				clicks[i] = KeyState.RELEASED;
			}
		}
	}

	public static boolean mouseDown(int keyCode) {
		return keyCode != -1 && (clicks[keyCode] == KeyState.ONCE || clicks[keyCode] == KeyState.PRESSED);
	}

	public static boolean mouseDownOnce(int keyCode) {
		return keyCode != -1 && (clicks[keyCode] == KeyState.ONCE);
	}



}
