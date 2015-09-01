package particleVisualization.rendering;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.vector.Vector4f;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.model.*;
import particleVisualization.util.MiscUtils;
import particleVisualization.util.ScreenshotUtil;


public class Scene {

	private static final float		NUMBER_OF_RENDER_SLICES	= 2;
	//public final static Vector4f	BG_COLOR	= new Vector4f(0.2f, 0.2f, 0.4f, 1.0f);
	public final static Vector4f	BG_COLOR				= new Vector4f(0.99f, 0.99f, 0.99f, 1.0f);
	//public final static Vector4f	BG_COLOR				= new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);
	public static float				FOG_DENSITY				= 1f;

	public static Camera			camera;
	private final InputManager		inputManager;
	private final HeadUpDisplay		hud;

	private final Shader			simpleTexturedShader, spriteShader, speedLineShader;
	private Shader					simpleFlatShader;

	private final Texture			particleTex, gridTex;
	private final Texture			trailTex;

	ParticleField					particleField;
	private final DrawableEntity	particleFieldSpeedLines, groundQuad;
	private DrawableEntity			exampleCube1, exampleCube2;

	boolean							drawGroundOrientation	= false;

	private boolean					bgColorInversed			= false;



	public Scene(int windowWidth, int windowHeight, MmpldData particleData) {

		inputManager = new InputManager(windowWidth, windowHeight);

		camera = new Camera(windowWidth, windowHeight, 40);
		camera.setFrontViewPos();
		//camera.rotate(31, -47, 0);

		trailTex = new Texture("speedlines.png");
		gridTex = new Texture("gray_grid_dark.jpg");
		particleTex = new Texture("blenderSphere.png");

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
		particleFieldSpeedLines = new ParticleFieldSpeedLines(particleField, trailTex);
		//		particleField.translate(-2, 0, 0);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//		glBlendFunc(GL_ONE, GL_ONE);
		glClearColor(BG_COLOR.x, BG_COLOR.y, BG_COLOR.z, BG_COLOR.w);
	}


	public void update() {
		float scaleStep = SimpleObjectViewer.getFrameTimeMs() / 1000.0f;
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
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_B)) {
			bgColorInversed = !bgColorInversed;
			if (bgColorInversed) {
				BG_COLOR.set(0.1f, 0.1f, 0.1f, 1.0f);
			}
			else {
				BG_COLOR.set(0.99f, 0.99f, 0.99f, 1.0f);
			}
			glClearColor(BG_COLOR.x, BG_COLOR.y, BG_COLOR.z, BG_COLOR.w);
		}

		if (InputManager.isKeyDown(GLFW.GLFW_KEY_O)) {
			FOG_DENSITY = MiscUtils.clamp(FOG_DENSITY * (1 + scaleStep) + scaleStep, 0, 15);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_L)) {
			FOG_DENSITY = MiscUtils.clamp(FOG_DENSITY * (1 - scaleStep) - scaleStep, 0, 15);
		}

		hud.update();
		HeadUpDisplay.putDebugValue(HudDebugKeys.fps, SimpleObjectViewer.getFpsAvg());
		camera.update();
		particleField.update(camera.getViewMatrix());
		particleFieldSpeedLines.update();
	}


	public void draw() {
		glEnable(GL_DEPTH_TEST);
		//		glDisable(GL_BLEND);

		if (drawGroundOrientation) {
			simpleTexturedShader.draw(camera.getViewMatrix(), groundQuad);
			//		simpleFlatShader.draw(camera.getViewMatrix(), exampleCube2);
		}

		//glEnable(GL_BLEND);
		//glDisable(GL_DEPTH_TEST);


		//		spriteShader.drawZSlice(camera.getViewMatrix(), 0f, 1f, particleField);
		//		speedLineShader.drawZSlice(camera.getViewMatrix(), 0f, 1f, particleFieldSpeedLines);

		for (float sliceIndex = 0; sliceIndex < NUMBER_OF_RENDER_SLICES; sliceIndex++) {
			spriteShader.drawZSlice(camera.getViewMatrix(), sliceIndex / NUMBER_OF_RENDER_SLICES, (sliceIndex + 1) / NUMBER_OF_RENDER_SLICES, particleField);
			speedLineShader.drawZSlice(camera.getViewMatrix(), sliceIndex / NUMBER_OF_RENDER_SLICES, (sliceIndex + 1) / NUMBER_OF_RENDER_SLICES, particleFieldSpeedLines);
		}


		hud.draw();

		//		if (SimpleObjectViewer.isGlfwVsynced()) {
		//			HeadUpDisplay.putDebugValue(HudDebugKeys.vSync, "glfw");
		//		} else {
		//			HeadUpDisplay.putDebugValue(HudDebugKeys.vSync, "custom");
		//			SyncUtil.sync(SimpleObjectViewer.refreshRate);
		//		}

		//		glAccum(GL_MULT, 0.8f);
		//		glAccum(GL_ACCUM, 0.2f);
		//		glAccum(GL_RETURN, 1);
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
