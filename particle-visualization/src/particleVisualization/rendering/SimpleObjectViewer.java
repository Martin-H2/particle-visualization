package particleVisualization.rendering;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class SimpleObjectViewer {

	private DisplayMode displayMode;
	private boolean shutdownRequest = false;

	private final Drawable sceneObject;


	public SimpleObjectViewer(Drawable sceneObject) {
		this.sceneObject = sceneObject;
	}

	public void setup() throws LWJGLException {
		createWindow();
		setupOpenGL();
	}

	public void mainloop() {
		while (!Display.isCloseRequested() && !shutdownRequest) {
			processInput();
			updateModel();
			drawObject();
		}
	}



	private void createWindow() throws LWJGLException {
		//System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");	//pseudo fullscreen for window mode
		displayMode = new DisplayMode(1280, 720);
		Display.setDisplayMode(displayMode);
		Display.setVSyncEnabled(true);
		Display.setTitle("Particle Visualization");
		Display.setLocation(20, 8);
		Display.create();
	}

	private void setupOpenGL() {
		//GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glClearDepth(1.0);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(
				45.0f,
				(float) displayMode.getWidth() / (float) displayMode.getHeight(),
				0.1f,
				100.0f);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
	}


	private void processInput() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) { shutdownRequest  = true; }

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) { sceneObject.resetTransRot(); }

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) { sceneObject.addPitch(-1); }
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) { sceneObject.addPitch(1); }

		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) { sceneObject.addYaw(-1); }
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) { sceneObject.addYaw(1); }

		//		float zoomModifier = -Mouse.getDWheel() / 12000f;
	}


	private void updateModel() {
		sceneObject.updateModel();
	}


	private void drawObject() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		sceneObject.draw();
		//Display.sync(60);
		Display.update();
	}


	public void cleanup() {
		//release textures...
		Display.destroy();
	}



}
