package particleVisualization.model;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glEnable;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.enums.RenderMode;
import particleVisualization.rendering.*;
import particleVisualization.util.MiscUtils;

public class ParticleField extends DrawableEntity {

	public final List<float[]>	dataFrames;
	public int					currentFrameIndex		= 0;
	private double				currentFrameIndexD		= 0;

	//private final Vector4f		globalRgba;
	public int					maxParticlesDisplayed	= 3;
	public final int			particlesPerFrame;
	private boolean				paused					= true;
	private float				dataFps					= SimpleObjectViewer.refreshRate;
	private int					uploadedFrames;
	private boolean				drawMiniPoints			= false;
	public int					speedLineLength			= 0;
	private float				speedLineLengthF		= 0;

	private final float			mouseSensitivity		= 0.15f;



	public ParticleField(MmpldData particleData, Texture spriteTexture) {
		super(spriteTexture, particleData.getDataFrames().get(0),
				particleData.getNumberOfDataFrames() * particleData.getParticlesPerFrame(),
				GL_POINTS, RenderMode.pointSprite);
		uploadedFrames = 1;
		dataFrames = particleData.getDataFrames();
		//globalRgba = particleData.getGlobalRgba();
		particlesPerFrame = particleData.getParticlesPerFrame();
		setBoundingBoxMin(particleData.getBoxMin());
		setBoundingBoxMax(particleData.getBoxMax());
		drawBoundingBox(true);
		glEnable(GL20.GL_POINT_SPRITE);
		glEnable(GL32.GL_PROGRAM_POINT_SIZE);
		//vertexArrayObject.setupIndirectBuffer();
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
	}

	@Override
	protected void drawVao(Shader shader) {
		//vertexArrayObject.drawIndirect();
		vertexArrayObject.draw(currentFrameIndex * particlesPerFrame, maxParticlesDisplayed, true);
		//			shader.setUniform1f(UniformName.spriteSize, 0.04f);

	}

	public void increaseMaxParticles(int maxParticlesInc) {
		maxParticlesDisplayed += maxParticlesInc;
	}



	@Override
	public void update() {
		if (dataFrames.size() > uploadedFrames) {
			vertexArrayObject.appendPositionData(dataFrames.get(uploadedFrames));
			uploadedFrames++;
		}

		if (!paused) {
			currentFrameIndexD += dataFps / SimpleObjectViewer.getFps();
			currentFrameIndex = (int) currentFrameIndexD;
		}



		//vertexArrayObject.testVboMapping(particlesPerFrame * 3); //566 fps



		float scaleStep = SimpleObjectViewer.getFrameTimeMs() / 1000.0f;


		if (InputManager.isKeyDown(GLFW.GLFW_KEY_KP_ADD)) {
			increaseMaxParticles(10);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_KP_SUBTRACT)) {
			increaseMaxParticles(-10);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_E)) {
			scaleClipped(1 + scaleStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_Q)) {
			scaleClipped(1 - scaleStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_F)) {
			speedLineLengthF = speedLineLengthF * (1 + scaleStep) + 10 * scaleStep;
			speedLineLength = (int) speedLineLengthF;
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_C)) {
			speedLineLengthF = speedLineLengthF * (1 - scaleStep) - 10 * scaleStep;
			speedLineLength = (int) speedLineLengthF;
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

		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_M)) {
			drawMiniPoints = !drawMiniPoints;
		}

		if (InputManager.isKeyDown(GLFW.GLFW_KEY_R)) {
			setPitch(0);
			setYaw(0);
			setRoll(0);
		}
		if (InputManager.isLockedOnLeftMouse()) {
			addYaw(InputManager.pollMouseXd() * -mouseSensitivity);
			float yd = InputManager.pollMouseYd();
			addPitch((float) (Math.cos(MiscUtils.degreesToRadians(Scene.camera.getYaw() - getYaw())) * (yd * -mouseSensitivity)));
			addRoll((float) (Math.sin(MiscUtils.degreesToRadians(Scene.camera.getYaw() - getYaw())) * (yd * mouseSensitivity)));
		}


		// PROTECTION
		maxParticlesDisplayed = MiscUtils.clip(maxParticlesDisplayed, 1, particlesPerFrame);
		speedLineLengthF = MiscUtils.clip(speedLineLengthF, 0, uploadedFrames - 100);
		speedLineLength = MiscUtils.clip(speedLineLength, 0, uploadedFrames - 100);
		if (currentFrameIndex >= uploadedFrames || currentFrameIndex < speedLineLength) {
			currentFrameIndexD = speedLineLength;
			currentFrameIndex = speedLineLength;
		}

		// HUD
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFps, paused ? 0 : dataFps);
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFrame, currentFrameIndex);
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFrameCount, dataFrames.size());
		HeadUpDisplay.putDebugValue(HudDebugKeys.numTailSegments, speedLineLength);
		HeadUpDisplay.putDebugValue(HudDebugKeys.numObjects, maxParticlesDisplayed * speedLineLength * (drawMiniPoints ? 2 : 1) + maxParticlesDisplayed);
	}

}
