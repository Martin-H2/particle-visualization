package particleVisualization.control;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.glfw.GLFWCursorPosCallback;

import particleVisualization.rendering.SimpleObjectViewer;


public class InputManager extends GLFWCursorPosCallback {

	private final static Set<Integer>	keysHoldDown		= new HashSet<Integer>();
	private static double				mouseX, mouseY, mouseXd, mouseYd;
	private static boolean				lockedCursorMode	= false;



	public InputManager(int windowWidth, int windowHeight) {
		glfwSetCursorPosCallback(SimpleObjectViewer.getWindowId(), this);
	}



	public static boolean isKeyDown(int glfwKey) {
		return glfwGetKey(SimpleObjectViewer.getWindowId(), glfwKey) == GLFW_PRESS;
	}

	public static boolean isKeyDownEvent(int glfwKey) {
		if (isKeyDown(glfwKey)) return keysHoldDown.add(glfwKey);
		else {
			keysHoldDown.remove(glfwKey);
			return false;
		}
	}



	public static boolean isMouseDown(int glfwMouseButton) {
		return glfwGetMouseButton(SimpleObjectViewer.getWindowId(), glfwMouseButton) == GLFW_PRESS;
	}

	public static double getMouseX() {
		return mouseX;
	}

	public static double getMouseY() {
		return mouseY;
	}

	public static float pollMouseXd() {
		float temp = (float) mouseXd;
		mouseXd = 0;
		return temp;
	}

	public static float pollMouseYd() {
		float temp = (float) mouseYd;
		mouseYd = 0;
		return temp;
	}



	public static void lockWindowCursor() {
		if (!lockedCursorMode) {
			//SimpleObjectViewer.centerMouse();
			glfwSetInputMode(SimpleObjectViewer.getWindowId(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
			glfwSetCursorPos(SimpleObjectViewer.getWindowId(), 0, 0);
			mouseXd = mouseYd = mouseX = mouseY = 0;
			lockedCursorMode = true;
		}
	}

	public static void unlockWindowCursor() {
		if (lockedCursorMode) {
			glfwSetInputMode(SimpleObjectViewer.getWindowId(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
			lockedCursorMode = false;
		}
	}

	public static boolean isLockedCursorMode() {
		return lockedCursorMode;
	}

	@Override
	public void invoke(long window, double xpos, double ypos) {
		//System.out.println("Mouse.xpos: " + xpos + " / Mouse.ypos: " + ypos);
		mouseXd += xpos - mouseX;
		mouseYd += ypos - mouseY;
		mouseX = xpos;
		mouseY = ypos;
	}

	public void cleanup() {
		this.release();
	}



}
