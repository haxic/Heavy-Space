package client.inputs;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWCursorPosCallback;

public class MousePositionHandler extends GLFWCursorPosCallback {
	private static Vector2f mousePosition = new Vector2f();
	private static Vector2f mouseDelta = new Vector2f();
	private static boolean cursorEnabled;

	private static Vector2f mouseDeltaTemp = new Vector2f();
	private static Vector2f mousePositionNow = new Vector2f();
	private static Vector2f mousePositionLast = new Vector2f();

	private static Vector2f temp = new Vector2f();
	@Override
	public void invoke(long window, double xpos, double ypos) {
		mousePositionLast.set(mousePositionNow);
		mousePositionNow.set((float) xpos, (float) ypos);
		mouseDeltaTemp.add(mousePositionNow.sub(mousePositionLast, temp));
	}
	
	public synchronized static void poll() {
		mousePosition.set(mousePositionNow);
		mousePositionNow.set(0);
		mousePositionLast.set(0);
		mouseDelta.set(mouseDeltaTemp);
		mouseDeltaTemp.set(0);
	}
	
	public static Vector2f getMousePosition() {
		return mousePosition;
	}
	
	public static Vector2f getMouseDelta() {
		return mouseDelta;
	}

	public static void setCursorVisibility(boolean cursorState) {
		cursorEnabled = cursorState;
	}
	
	public static boolean isCursorEnabled() {
		return cursorEnabled;
	}


}
