package particleVisualization.rendering;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.glfw.GLFW;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.model.*;
import particleVisualization.util.ScreenshotUtil;


public class Scene {

	public static Camera		camera;
	private final InputManager	inputManager;
	private final HeadUpDisplay	hud;

	private final Shader		simpleTexturedShader, spriteShader, speedLineShader;
	private Shader				simpleFlatShader;

	private final Texture		particleTex, gridTex;
	private Texture				crateTex;

	ParticleField				particleField;
	private final DrawableEntity	particleFieldSpeedLines, groundQuad;
	private DrawableEntity			exampleCube1, exampleCube2;

	boolean							drawGroundOrientation	= true;



	public Scene(int windowWidth, int windowHeight, MmpldData particleData) {

		inputManager = new InputManager(windowWidth, windowHeight);

		camera = new Camera(windowWidth, windowHeight, 40);
		camera.setFrontViewPos();
		//camera.rotate(31, -47, 0);

		//		crateTex = new Texture("crate.jpg");
		gridTex = new Texture("gray_grid_dark.jpg");
		particleTex = new Texture("MegamolBubble.png");

		hud = new HeadUpDisplay(windowWidth, windowHeight);

		simpleTexturedShader = new Shader(camera.getProjectionMatrix(), "ModelViewProjection_vs.glsl", "Textured_fs.glsl");
		spriteShader = new Shader(camera.getProjectionMatrix(), "Sprite_vs.glsl", "Sprite_fs.glsl");
		speedLineShader = new Shader(camera.getProjectionMatrix(), "SpeedLine_vs.glsl", "SpeedLine_gs.glsl", "SpeedLine_fs.glsl");
		//		simpleFlatShader = new Shader(camera.getProjectionMatrix(), "ModelViewProjection_vs.glsl", "DirectionalFlatShading_fs.glsl");

		groundQuad = new Quad(gridTex);
		groundQuad.translate(0, -5, 0);
		groundQuad.setScale(20);
		//
		//		exampleCube1 = new CubeQuads(crateTex);
		//		exampleCube2 = new CubeQuads(crateTex);
		//		exampleCube2.translate(2, 0, 0);

		particleField = new ParticleField(particleData, particleTex);
		particleFieldSpeedLines = new ParticleFieldSpeedLines(particleField);
		//		particleField.translate(-2, 0, 0);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//		glBlendFunc(GL_ONE, GL_ONE);
	}


	public void update() {
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PRINT_SCREEN)) {
			ScreenshotUtil.savePngScreenShot();
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
			glfwSetWindowShouldClose(SimpleObjectViewer.getWindowId(), GL_TRUE);
			return;
		}
		if (InputManager.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT) || InputManager.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			InputManager.lockWindowCursor();
		}
		else {
			InputManager.unlockWindowCursor();
		}
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_G)) {
			drawGroundOrientation = !drawGroundOrientation;
		}
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_V)) {
			SimpleObjectViewer.toggleVsync();
		}
		hud.update();
		HeadUpDisplay.putDebugValue(HudDebugKeys.fps, SimpleObjectViewer.getFpsAvg());
		camera.update();
		particleField.update();
		//groundQuad.update();
	}


	public void draw() {
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);

		if (drawGroundOrientation) {
			simpleTexturedShader.draw(camera.getViewMatrix(), groundQuad);
			//		simpleFlatShader.draw(camera.getViewMatrix(), exampleCube2);
		}

		//glEnable(GL_BLEND);
		//glDisable(GL_DEPTH_TEST);
		spriteShader.draw(camera.getViewMatrix(), particleField);
		speedLineShader.draw(camera.getViewMatrix(), particleFieldSpeedLines);

		hud.draw();

		//		if (SimpleObjectViewer.isGlfwVsynced()) {
		//			HeadUpDisplay.putDebugValue(HudDebugKeys.vSync, "glfw");
		//		} else {
		//			HeadUpDisplay.putDebugValue(HudDebugKeys.vSync, "custom");
		//			SyncUtil.sync(SimpleObjectViewer.refreshRate);
		//		}

	}

	public void destroy() {
		//		exampleCube1.destroy();
		spriteShader.destroy();
		speedLineShader.destroy();
		simpleTexturedShader.destroy();
		groundQuad.destroy();
		inputManager.cleanup();
	}

}
