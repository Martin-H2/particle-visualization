package particleVisualization.model;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector4f;

import particleVisualization.control.InputManager;
import particleVisualization.rendering.HeadUpDisplay;
import particleVisualization.rendering.HudDebugKeys;
import particleVisualization.rendering.SimpleObjectViewer;
import particleVisualization.rendering.Texture;

public class ParticleField extends DrawableEntity {

	private final List<float[]> dataFrames;
	private int currentFrameIndex = 0;
	private double currentFrameIndexD = 0;

	private final Vector4f globalRgba;
	private int maxParticlesDisplayed = 1000;
	private final int maxParticlesPerFrame;
	private int numberOfDrawnObjects;
	private boolean paused = false;
	int dataFps = 60;
	int uploadedFrames;



	public ParticleField(MmpldData particleData, Texture spriteTexture) {
		super(spriteTexture, particleData.getDataFrames().get(0), particleData.getNumberOfDataFrames()*particleData.getMaxParticlesPerFrame(), GL11.GL_POINTS);
		uploadedFrames = 1;
		dataFrames = particleData.getDataFrames();
		globalRgba = particleData.getGlobalRgba();
		maxParticlesPerFrame = particleData.getMaxParticlesPerFrame();
		setBoundingBoxMin(particleData.getBoxMin());
		setBoundingBoxMax(particleData.getBoxMax());
	}

	public void increaseMaxParticles(int maxParticlesInc) {
		maxParticlesDisplayed += maxParticlesInc;
		if (maxParticlesDisplayed<0) {
			maxParticlesDisplayed=1;
		}
		if (maxParticlesDisplayed>maxParticlesPerFrame) {
			maxParticlesDisplayed=maxParticlesPerFrame;
		}
	}

	@Override
	public void update() {
		//TODO send new dataFrames to GPU
		if (dataFrames.size() > uploadedFrames) {
			vertexArrayObject.appendPositionData(dataFrames.get(uploadedFrames));
			uploadedFrames++;
		}

		if (!paused) {
			currentFrameIndexD = (currentFrameIndexD + dataFps/SimpleObjectViewer.getFps()) % dataFrames.size();
			currentFrameIndex = (int) currentFrameIndexD;
		}

		float translationStep = SimpleObjectViewer.getFrameTimeMs() / 200.0f;
		float rotationStep = SimpleObjectViewer.getFrameTimeMs() / 10.0f;

		if (InputManager.isKeyDown(GLFW.GLFW_KEY_UP)) {
			translate(0, 0, translationStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
			translate(0, 0, -translationStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
			translate(translationStep, 0, 0);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
			translate(-translationStep, 0, 0);
		}


		if (InputManager.isKeyDown(GLFW.GLFW_KEY_PAGE_UP)) {
			//			translate(0, translationStep, 0);
			rotate(0, 0, rotationStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN)) {
			//			translate(0, -translationStep, 0);
			rotate(0, 0, -rotationStep);
		}


		if (InputManager.isKeyDown(GLFW.GLFW_KEY_ENTER)) {
			addScale(translationStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_BACKSPACE)) {
			addScale(-translationStep);
		}


		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFps, dataFps);
		HeadUpDisplay.putDebugValue(HudDebugKeys.frameNumber, currentFrameIndex);
		HeadUpDisplay.putDebugValue(HudDebugKeys.numFrames, dataFrames.size());
		HeadUpDisplay.putDebugValue(HudDebugKeys.numParticles, dataFrames.get(0).length/3);
	}

}
