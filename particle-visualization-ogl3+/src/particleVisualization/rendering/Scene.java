package particleVisualization.rendering;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.glfw.GLFW;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.model.Camera;
import particleVisualization.model.DrawableEntity;
import particleVisualization.model.MmpldData;
import particleVisualization.model.ParticleField;
import particleVisualization.util.ScreenshotUtil;


public class Scene {

	private final Camera			camera;
	private final InputManager		inputManager;
	private final HeadUpDisplay		hud;

	private Shader					simpleTexturedShader, simpleFlatShader;
	private final Shader			spriteShader;
	private final Texture			particleTex;
	private Texture					gridTex;
	private Texture					crateTex;
	private final DrawableEntity	particleField;
	private DrawableEntity			exampleCube1;
	private DrawableEntity			exampleCube2;
	private DrawableEntity			exampleQuad;


	public Scene(int windowWidth, int windowHeight, MmpldData particleData) {

		inputManager = new InputManager(windowWidth, windowHeight);

		camera = new Camera(windowWidth, windowHeight, 40);
		camera.translate(4, 2.5f, -3);
		camera.rotate(31, -47, 0);

		//		crateTex = new Texture("crate.jpg");
		//		gridTex = new Texture("stGrid1.png");
		particleTex = new Texture("MegamolBubble.png");

		hud = new HeadUpDisplay(windowWidth, windowHeight);

		//		simpleTexturedShader = new Shader(camera.getProjectionMatrix(), "ModelViewProjection_vs.glsl", "Textured_fs.glsl");
		spriteShader = new Shader(camera.getProjectionMatrix(), "Sprite_vs.glsl", "Sprite_fs.glsl");
		//		simpleFlatShader = new Shader(camera.getProjectionMatrix(), "ModelViewProjection_vs.glsl", "DirectionalFlatShading_fs.glsl");

		//		exampleQuad = new Quad(gridTex);
		//		exampleQuad.translate(0, -0.6f, 0);
		//		exampleQuad.setScale(10);
		//
		//		exampleCube1 = new CubeQuads(crateTex);
		//		exampleCube2 = new CubeQuads(crateTex);
		//		exampleCube2.translate(2, 0, 0);

		particleField = new ParticleField(particleData, particleTex);
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
		}
		if (InputManager.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
			InputManager.lockWindowCursor();
		}
		else {
			InputManager.unlockWindowCursor();
		}
		HeadUpDisplay.putDebugValue(HudDebugKeys.fps, SimpleObjectViewer.getFpsAvg());
		camera.update();
		particleField.update();
	}


	public void draw() {
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);
		//		simpleTexturedShader.draw(camera.getViewMatrix(), exampleQuad, exampleCube1);
		//		simpleFlatShader.draw(camera.getViewMatrix(), exampleCube2);

		//glEnable(GL_BLEND);
		//glDisable(GL_DEPTH_TEST);
		spriteShader.draw(camera.getViewMatrix(), particleField);

		hud.draw();
	}


	public void destroy() {
		//		exampleCube1.destroy();
		spriteShader.destroy();
		//		exampleQuad.destroy();
		inputManager.cleanup();
	}

}