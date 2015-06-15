package particleVisualization.model;

import java.util.List;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.vector.Vector4f;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.enums.RenderMode;
import particleVisualization.rendering.*;
import particleVisualization.util.MiscUtils;

public class ParticleField extends DrawableEntity {

	private final List<float[]>	dataFrames;
	private int					currentFrameIndex		= 0;
	private double				currentFrameIndexD		= 0;

	private final Vector4f		globalRgba;
	private final int			maxParticlesDisplayed	= 1000;
	private final int			particlesPerFrame;
	private int					numberOfDrawnFrames		= 1;
	private float				tailLength				= 0;
	private boolean				paused					= true;
	private float				dataFps					= SimpleObjectViewer.refreshRate;
	private int					uploadedFrames;

	private final float			mouseSensitivity		= 0.15f;


	public ParticleField(MmpldData particleData, Texture spriteTexture) {
		super(spriteTexture, particleData.getDataFrames().get(0), particleData.getNumberOfDataFrames() * particleData.getParticlesPerFrame(), GL11.GL_POINTS, RenderMode.textured);
		uploadedFrames = 1;
		dataFrames = particleData.getDataFrames();
		globalRgba = particleData.getGlobalRgba();
		particlesPerFrame = particleData.getParticlesPerFrame();
		setBoundingBoxMin(particleData.getBoxMin());
		setBoundingBoxMax(particleData.getBoxMax());
		drawBoundingBox(true);
		GL11.glEnable(GL20.GL_POINT_SPRITE);
		GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
		vertexArrayObject.setupIndirectBuffer();
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
	}

	@Override
	protected void drawVao() {
		//vertexArrayObject.drawIndirect();
		vertexArrayObject.draw(currentFrameIndex * particlesPerFrame, numberOfDrawnFrames * particlesPerFrame, true);
	}

	//	public void increaseMaxParticles(int maxParticlesInc) {
	//		maxParticlesDisplayed += maxParticlesInc;
	//		if (maxParticlesDisplayed < 0) {
	//			maxParticlesDisplayed = 1;
	//		}
	//		if (maxParticlesDisplayed > particlesPerFrame) {
	//			maxParticlesDisplayed = particlesPerFrame;
	//		}
	//	}



	@Override
	public void update() {
		//TODO send new dataFrames to GPU
		if (dataFrames.size() > uploadedFrames) {
			vertexArrayObject.appendPositionData(dataFrames.get(uploadedFrames));
			uploadedFrames++;
		}

		if (!paused) {
			currentFrameIndexD = (currentFrameIndexD + dataFps / SimpleObjectViewer.getFps()) % uploadedFrames;
			currentFrameIndex = (int) currentFrameIndexD;
		}

		float scaleStep = SimpleObjectViewer.getFrameTimeMs() / 1000.0f;


		if (InputManager.isKeyDown(GLFW.GLFW_KEY_E)) {
			scaleClipped(1 + scaleStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_Q)) {
			scaleClipped(1 - scaleStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_F)) {
			tailLength = MiscUtils.clip(tailLength * (1 + scaleStep) + 10 * scaleStep, 0, uploadedFrames - 1);
			numberOfDrawnFrames = (int) tailLength + 1;
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_C)) {
			tailLength = MiscUtils.clip(tailLength * (1 - scaleStep) - 10 * scaleStep, 0, uploadedFrames - 1);
			numberOfDrawnFrames = (int) tailLength + 1;
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_X)) {
			dataFps = MiscUtils.clip(dataFps * (1 + scaleStep), 1, 1000);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_Z)) {
			dataFps = MiscUtils.clip(dataFps * (1 - scaleStep), 1, 1000);
		}
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_TAB)) {
			paused = !paused;
		}

		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_B)) {
			toggleBoundingBox();
		}

		if (InputManager.isKeyDown(GLFW.GLFW_KEY_R)) {
			setPitch(0);
			setYaw(0);
			setRoll(0);
		}
		//TODO mouseRot
		if (InputManager.isLockedOnLeftMouse()) {
			addYaw(InputManager.pollMouseXd() * -mouseSensitivity);
			float yd = InputManager.pollMouseYd();
			addPitch((float) (Math.cos(MiscUtils.degreesToRadians(Scene.camera.getYaw() - getYaw())) * (yd * -mouseSensitivity)));
			addRoll((float) (Math.sin(MiscUtils.degreesToRadians(Scene.camera.getYaw() - getYaw())) * (yd * mouseSensitivity)));
		}



		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFps, paused ? 0 : dataFps);
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFrame, currentFrameIndex);
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFrameCount, dataFrames.size());
		HeadUpDisplay.putDebugValue(HudDebugKeys.numTailSegments, numberOfDrawnFrames - 1);
		HeadUpDisplay.putDebugValue(HudDebugKeys.numObjects, particlesPerFrame * numberOfDrawnFrames);
	}

}
