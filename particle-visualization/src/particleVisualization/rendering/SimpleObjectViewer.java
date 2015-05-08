package particleVisualization.rendering;

import java.awt.Font;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import particleVisualization.util.Stopwatch;

public class SimpleObjectViewer {

	private final int wWidth;
	private final int wHeight;
	private DisplayMode displayMode;
	private boolean shutdownRequest = false;

	private final ParticleField particleField;
	private UnicodeFont uFont;
	private int overlayLeftMargin;

	private double currentFps;
	private long framesRendered;
	private Stopwatch fpsStopwatch;


	public SimpleObjectViewer(int windowWidth, int windowHeight, ParticleField sceneObject) {
		particleField = sceneObject;
		wWidth = windowWidth;
		wHeight = windowHeight;
		overlayLeftMargin = wWidth-130;
	}

	@SuppressWarnings("unchecked")
	public void setup() throws LWJGLException {
		createWindow();
		setupOpenGL();

		//unicode font
		Font f = new Font("Arial", Font.PLAIN, 14);
		uFont = new UnicodeFont(f);
		uFont.addAsciiGlyphs();
		uFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
		try {
			uFont.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		}

	}

	public void mainloop() {
		fpsStopwatch = new Stopwatch();
		while (!Display.isCloseRequested() && !shutdownRequest) {
			processInput();
			updateModel();
			draw();
			updateFps();
		}
	}



	private void updateFps() {
		framesRendered++;
		if (framesRendered%10 == 0) {
			currentFps = 1.0 / (fpsStopwatch.getElapsedSeconds() / 10.0);
			fpsStopwatch.restart();
		}
	}
	private double getFps() {
		return currentFps;
	}

	private void createWindow() throws LWJGLException {
		//System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");	//pseudo fullscreen for window mode
		displayMode = new DisplayMode(wWidth, wHeight);
		Display.setDisplayMode(displayMode);
		//Display.setVSyncEnabled(true);
		Display.setTitle("Particle Visualization");
		Display.setLocation(20, 8);
		Display.create();
	}

	private void setupOpenGL() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
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
				(float) wWidth / (float) wHeight,
				0.1f,
				100.0f);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
	}


	private void processInput() {
		// Keyboard
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) { shutdownRequest  = true; }

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) { particleField.resetTransRot(); }

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) { particleField.addPitch(-1); }
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) { particleField.addPitch(1); }

		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) { particleField.addYaw(-1); }
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) { particleField.addYaw(1); }

		if (Keyboard.isKeyDown(Keyboard.KEY_I)) { particleField.increaseMaxParticles(40); }
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) { particleField.increaseMaxParticles(-40); }

		// Mouse
		if (Mouse.isButtonDown(0)) {
			particleField.addPitch(Mouse.getDX()/3.0f);
			particleField.addYaw(-Mouse.getDY()/3.0f);
		}

		particleField.translate(0, 0, Mouse.getDWheel()/800.0f);
	}


	private void updateModel() {
		particleField.updateModel();
	}


	private void draw() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		particleField.draw();
		drawOverlay();
		Display.sync(60);
		Display.update();
	}

	private void drawOverlay() {

		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0, wWidth, wHeight, 0, 1, -1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
		GL11.glVertex2f(overlayLeftMargin, 0);
		GL11.glVertex2f(overlayLeftMargin, 50);
		GL11.glVertex2f(wWidth, 50);
		GL11.glVertex2f(wWidth, 0);
		GL11.glEnd();

		uFont.drawString(overlayLeftMargin+5, 10, "fps: " + Math.round(getFps()));
		uFont.drawString(overlayLeftMargin+5, 25, "particles: " + particleField.getParticleCount());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();

		GL11.glEnable(GL11.GL_DEPTH_TEST);

	}

	public void cleanup() {
		//release textures...
		Display.destroy();
	}



}
