package client.inputs;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyboardHandler extends GLFWKeyCallback {

	private static final int KB_KEY_COUNT = 65536;
	public static boolean[] KB_currentKeys = new boolean[KB_KEY_COUNT];
	private static KeyState[] KB_keys = new KeyState[KB_KEY_COUNT];

	public void invoke(long window, int key, int scancode, int action, int mods) {
		if (key >= 0 && key < KB_KEY_COUNT) {
			KB_currentKeys[key] = action != GLFW.GLFW_RELEASE;
		}
	}

	private enum KeyState {
		RELEASED, // Not down
		PRESSED, // Down, but not the first time
		ONCE // Down for the first time
	}

	public synchronized static void poll() {
		for (int i = 0; i < KB_KEY_COUNT; ++i) {
			if (KB_currentKeys[i]) {
				if (KB_keys[i] == KeyState.RELEASED) {
					KB_keys[i] = KeyState.ONCE;
				} else
					KB_keys[i] = KeyState.PRESSED;
			} else {
				KB_keys[i] = KeyState.RELEASED;
			}
		}
	}

	public static boolean kb_keyDown(int keyCode) {
		return KB_keys[keyCode] == KeyState.ONCE || KB_keys[keyCode] == KeyState.PRESSED;
	}

	public static boolean kb_keyDownOnce(int keyCode) {
		return KB_keys[keyCode] == KeyState.ONCE;
	}

}
