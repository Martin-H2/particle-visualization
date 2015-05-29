package particleVisualization.rendering;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

import org.lwjgl.Sys;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.libffi.Closure;

import particleVisualization.model.MmpldData;

public class SimpleObjectViewer {

	private static final boolean	DEBUG_MODE		= true;

	private static int				windowWidth;
	private static int				windowHeight;
	private final String			windowTitle;

	private static long				windowId;

	private GLFWErrorCallback		errorCallback;
	private Closure					debugMessageCallback;
	private long					frameStartNano	= -1L;
	private static float			frameTimeMs		= 16.6f;
	private int 					avgFpsFrameCounter = 0;
	private static float			avgFpsFrameTimeCuml = 0;
	private static float			fpsAvg		= 60f;

	private Scene					scene;



	public SimpleObjectViewer(int windowWidth, int windowHeight, String windowTitle) {
		SimpleObjectViewer.windowWidth = windowWidth;
		SimpleObjectViewer.windowHeight = windowHeight;
		this.windowTitle = windowTitle;
	}

	// ==================================== SETUP ====================================
	public void setup(MmpldData particleData) {
		System.out.println("\nStarting renderer...\nlwjgl version: " + Sys.getVersion());
		setupWindow();
		setupOpenGL();
		setupScene(particleData);
	}

	private void setupWindow() {
		glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));
		if (glfwInit() != GL_TRUE) throw new RuntimeException("Unable to initialize GLFW");
		//		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		//		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		//		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); //TODO use core profile ?
		//		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		windowId = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);
		if (windowId == NULL) throw new RuntimeException("Failed to create the GLFW window");
		ByteBuffer vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(windowId, (GLFWvidmode.width(vidMode) - windowWidth) / 2, (GLFWvidmode.height(vidMode) - windowHeight) / 2);
		glfwMakeContextCurrent(windowId);
		glfwSwapInterval(0); // 1 == v-sync
	}

	private void setupOpenGL() {
		GLContext context = GLContext.createFromCurrent();
		System.out.println("openGL version: " + glGetString(GL_VERSION));
		if (DEBUG_MODE) {
			glEnable(GL43.GL_DEBUG_OUTPUT);
			debugMessageCallback = context.setupDebugMessageCallback(System.out);
			if (!context.getCapabilities().GL_AMD_debug_output && !context.getCapabilities().GL_ARB_debug_output && !context.getCapabilities().GL_KHR_debug) {
				System.out.println("no ARB/AMD/KHR debug output capabilities");
			}
		}
		glViewport(0, 0, windowWidth, windowHeight);
		glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		glEnable(GL_DEPTH_TEST);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		//		glEnable(GL_POLYGON_SMOOTH);
		//		glEnable(GL_LINE_SMOOTH);
		//		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
		//		glShadeModel(GL_SMOOTH);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
	}


	private void setupScene(MmpldData particleData) {
		GLFW.glfwSetInputMode(windowId, GLFW_STICKY_KEYS, GL_TRUE);
		GLFW.glfwSetInputMode(windowId, GLFW_STICKY_MOUSE_BUTTONS, GL_TRUE);
		scene = new Scene(windowWidth, windowHeight, particleData);
	}

	public void cleanup() {
		scene.destroy();
		if (errorCallback != null) {
			errorCallback.release();
		}
		if (debugMessageCallback != null) {
			debugMessageCallback.release();
		}
		glfwTerminate();
	}



	// ==================================== EXECUTION ====================================
	public void run() {
		while (glfwWindowShouldClose(windowId) == GL_FALSE) {
			frameTiming();
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glfwPollEvents();
			scene.update();
			scene.draw();
			glfwSwapBuffers(windowId);
		}
	}

	private void frameTiming() {
		if (frameStartNano != -1L) {
			frameTimeMs = (System.nanoTime() - frameStartNano) / 1000000f;
			avgFpsFrameCounter++;
			avgFpsFrameTimeCuml += frameTimeMs;
			if (avgFpsFrameTimeCuml >= 1000f) {
				fpsAvg = 1f / (avgFpsFrameTimeCuml / avgFpsFrameCounter) * 1000f;
				//System.out.println(getFpsAvg());
				avgFpsFrameCounter = 0;
				avgFpsFrameTimeCuml = 0;
			}
		}
		frameStartNano = System.nanoTime();
	}

	public static long getWindowId() {
		return windowId;
	}

	public static float getFrameTimeMs() {
		return frameTimeMs;
	}

	public static void centerMouse() {
		glfwSetCursorPos(windowId, windowWidth / 2d, windowHeight / 2d);
	}

	public static float getFps() {
		return 1f / (getFrameTimeMs() / 1000f);
	}

	public static int getFpsAvg() {
		return Math.round(fpsAvg);
	}

	private static void enableOutlineMode(boolean lineMode) {
		if (lineMode) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		}
		else {
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		}
	}
}
