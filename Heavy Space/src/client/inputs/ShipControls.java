package client.inputs;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.display.DisplayManager;

public class ShipControls {
	private static final int Horitonzal = 0, Vertical = 1, Null = -1;

	private static int KB_FORWARD_THRUST = GLFW.GLFW_KEY_W;
	private static int KB_REVERSE_THRUST = GLFW.GLFW_KEY_S;
	private static int KB_PORT_THRUST = GLFW.GLFW_KEY_A;
	private static int KB_STARBOARD_THRUST = GLFW.GLFW_KEY_D;
	private static int KB_ASCEND = GLFW.GLFW_KEY_LEFT_CONTROL;
	private static int KB_DECEND = GLFW.GLFW_KEY_LEFT_SHIFT;

	private static int KB_YAW_LEFT = Null;
	private static int KB_YAW_RIGHT = Null;
	private static int KB_PITCH_UP = Null;
	private static int KB_PITCH_DOWN = Null;
	private static int KB_ROLL_RIGHT = GLFW.GLFW_KEY_Q;
	private static int KB_ROLL_LEFT = GLFW.GLFW_KEY_E;

	private float KB_SENSITIVITY_YAW = 1f;
	private float KB_SENSITIVITY_PITCH = 1f;
	private float KB_SENSITIVITY_ROLL = 1f;

	private static int KB_FIRE_PRIMARY = Null;
	private static int KB_FIRE_SECONDARY = Null;

	private static int MB_FIRE_PRIMARY = GLFW.GLFW_MOUSE_BUTTON_1;
	private static int MB_FIRE_SECONDARY = GLFW.GLFW_MOUSE_BUTTON_2;

	private static int MD_YAW = Vertical;
	private static int MD_PITCH = Horitonzal;
	private static int MD_ROLL = Null;

	private float MOUSE_SENSITIVITY_HORIZONTAL = 0.25f;
	private float MOUSE_SENSITIVITY_VERTICAL = 0.25f;

	private float MOUSE_INVERT_HORIZONTAL = 1f;
	private float MOUSE_INVERT_VERTICAL = -1f;

	// public Vector3f linearDirection = new Vector3f();
	public Vector3f angularDirection = new Vector3f();

	public boolean primary;
	public boolean secondary;

	public boolean forwardThrust;
	public boolean reverseThrust;
	public boolean starboardThrust;
	public boolean portThrust;
	public boolean ascend;
	public boolean decend;

	public void process() {
		// linearDirection.set(0, 0, 0);
		angularDirection.set(0, 0, 0);
		forwardThrust = false;
		reverseThrust = false;
		starboardThrust = false;
		portThrust = false;
		ascend = false;
		decend = false;
		primary = false;
		secondary = false;

		Vector2f mousePositionDelta = DisplayManager.getMousePositionDelta();
		if (!DisplayManager.isCursorEnabled()) {
			switch (MD_YAW) {
			case Horitonzal:
				angularDirection.x += mousePositionDelta.x * MOUSE_INVERT_HORIZONTAL * MOUSE_SENSITIVITY_HORIZONTAL;
				break;
			case Vertical:
				angularDirection.x += mousePositionDelta.y * MOUSE_INVERT_VERTICAL * MOUSE_SENSITIVITY_VERTICAL;
				break;
			default:
				break;
			}
			switch (MD_PITCH) {
			case Horitonzal:
				angularDirection.y += mousePositionDelta.x * MOUSE_INVERT_HORIZONTAL * MOUSE_SENSITIVITY_HORIZONTAL;
				break;
			case Vertical:
				angularDirection.y += mousePositionDelta.y * MOUSE_INVERT_VERTICAL * MOUSE_SENSITIVITY_VERTICAL;
				break;
			default:
				break;
			}
			switch (MD_ROLL) {
			case Horitonzal:
				angularDirection.z += mousePositionDelta.x * MOUSE_INVERT_HORIZONTAL * MOUSE_SENSITIVITY_HORIZONTAL;
				break;
			case Vertical:
				angularDirection.z += mousePositionDelta.y * MOUSE_INVERT_VERTICAL * MOUSE_SENSITIVITY_VERTICAL;
				break;
			default:
				break;
			}
		}

		if (MouseHandler.mouseDown(MB_FIRE_PRIMARY))
			primary = true;
		if (MouseHandler.mouseDownOnce(MB_FIRE_SECONDARY))
			secondary = true;

		// if (KeyboardHandler.kb_keyDown(KB_FORWARD_THRUST))
		// linearDirection.z++;
		// if (KeyboardHandler.kb_keyDown(KB_REVERSE_THRUST))
		// linearDirection.z---;
		// if (KeyboardHandler.kb_keyDown(KB_STARBOARD_THRUST))
		// linearDirection.x++;
		// if (KeyboardHandler.kb_keyDown(KB_PORT_THRUST))
		// linearDirection.x--;
		// if (KeyboardHandler.kb_keyDown(KB_ASCEND))
		// linearDirection.y++;
		// if (KeyboardHandler.kb_keyDown(KB_DECEND))
		// linearDirection.y--;
		//
		// if (linearDirection.length() > 0)
		// linearDirection.normalize();
		//
		// if (KeyboardHandler.kb_keyDown(KB_YAW_RIGHT))
		// angularDirection.x += KB_SENSITIVITY_YAW;
		// if (KeyboardHandler.kb_keyDown(KB_YAW_LEFT))
		// angularDirection.x -= KB_SENSITIVITY_YAW;
		// if (KeyboardHandler.kb_keyDown(KB_PITCH_UP))
		// angularDirection.y += KB_SENSITIVITY_PITCH;
		// if (KeyboardHandler.kb_keyDown(KB_PITCH_DOWN))
		// angularDirection.y -= KB_SENSITIVITY_PITCH;
		// if (KeyboardHandler.kb_keyDown(KB_ROLL_RIGHT))
		// angularDirection.z += KB_SENSITIVITY_ROLL;
		// if (KeyboardHandler.kb_keyDown(KB_ROLL_LEFT))
		// angularDirection.z -= KB_SENSITIVITY_ROLL;

		if (KeyboardHandler.kb_keyDown(KB_FORWARD_THRUST))
			forwardThrust = true;
		if (KeyboardHandler.kb_keyDown(KB_REVERSE_THRUST))
			reverseThrust = true;
		if (KeyboardHandler.kb_keyDown(KB_STARBOARD_THRUST))
			starboardThrust = true;
		if (KeyboardHandler.kb_keyDown(KB_PORT_THRUST))
			portThrust = true;
		if (KeyboardHandler.kb_keyDown(KB_ASCEND))
			ascend = true;
		if (KeyboardHandler.kb_keyDown(KB_DECEND))
			decend = true;

		// if (linearDirection.length() > 0)
		// linearDirection.normalize();

		if (KeyboardHandler.kb_keyDown(KB_YAW_RIGHT))
			angularDirection.x += KB_SENSITIVITY_YAW;
		if (KeyboardHandler.kb_keyDown(KB_YAW_LEFT))
			angularDirection.x -= KB_SENSITIVITY_YAW;
		if (KeyboardHandler.kb_keyDown(KB_PITCH_UP))
			angularDirection.y += KB_SENSITIVITY_PITCH;
		if (KeyboardHandler.kb_keyDown(KB_PITCH_DOWN))
			angularDirection.y -= KB_SENSITIVITY_PITCH;
		if (KeyboardHandler.kb_keyDown(KB_ROLL_RIGHT))
			angularDirection.z += KB_SENSITIVITY_ROLL;
		if (KeyboardHandler.kb_keyDown(KB_ROLL_LEFT))
			angularDirection.z -= KB_SENSITIVITY_ROLL;

		if (KeyboardHandler.kb_keyDown(KB_FIRE_PRIMARY))
			primary = true;
		if (KeyboardHandler.kb_keyDownOnce(KB_FIRE_SECONDARY))
			secondary = true;
	}

	// public Vector3f getLinearDirection() {
	// return linearDirection;
	// }

}
