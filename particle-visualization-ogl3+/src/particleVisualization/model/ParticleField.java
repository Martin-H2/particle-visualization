package particleVisualization.model;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import java.nio.FloatBuffer;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.enums.RenderMode;
import particleVisualization.enums.ShaderLayout;
import particleVisualization.enums.UniformName;
import particleVisualization.rendering.*;
import particleVisualization.util.MiscUtils;
import particleVisualization.util.VertexSorter;

public class ParticleField extends DrawableEntity {

	private FloatBuffer			particleBuffer;


	public final List<float[]>	dataFrames;
	private final List<float[]>	dataFramesColors;
	public int					currentFrameIndex	= 0;
	private double				currentFrameIndexD	= 0;

	//private final Vector4f		globalRgba;
	public int					maxParticlesDisplayed;
	private float				maxParticlesDisplayedF;
	public final int			particlesPerFrame;
	private boolean				paused				= true;
	private float				dataFps				= SimpleObjectViewer.refreshRate;
	private int					uploadedFrames;
	private boolean				drawMiniPoints		= false;
	public int					speedLineLength		= 0;
	private float				speedLineLengthF	= 0;

	private final float			mouseSensitivity	= 0.15f;
	public float				globalRadius;
	public final MmpldData		particleData;
	private Vector3f			jumpThresholds;


	private final Matrix4f		modelViewMatrix		= new Matrix4f();



	public ParticleField(MmpldData particleData, Texture spriteTexture) {
		super(spriteTexture, particleData.getDataFrames().get(0),
				particleData.isColorDataSet() ? particleData.getDataFramesColors().get(0) : null,
				particleData.getNumberOfDataFrames() * particleData.getParticlesPerFrame(), GL_POINTS,
				particleData.isColorDataSet() ? RenderMode.texturedAndColored : RenderMode.textured);
		uploadedFrames = 1;
		dataFrames = particleData.getDataFrames();
		this.particleData = particleData;
		dataFramesColors = particleData.getDataFramesColors();
		//globalRgba = particleData.getGlobalRgba();
		particlesPerFrame = particleData.getParticlesPerFrame();
		globalRadius = particleData.getGlobalRadius();
		setBoundingBoxMin(particleData.getBoxMin());
		setBoundingBoxMax(particleData.getBoxMax());
		drawBoundingBox(true);
		glEnable(GL20.GL_POINT_SPRITE);
		glEnable(GL32.GL_PROGRAM_POINT_SIZE);
		maxParticlesDisplayed = particlesPerFrame;
		maxParticlesDisplayedF = particlesPerFrame;
		//vertexArrayObject.setupIndirectBuffer();
		resetScalePos();
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
		shader.setUniform1f(UniformName.spriteSize, globalRadius * 2f);
		shader.setUniform4f(UniformName.fogColor, Scene.BG_COLOR);
		shader.setUniform1f(UniformName.fogDensity, Scene.FOG_DENSITY);
	}

	@Override
	protected void drawVao(Shader shader, float startFraction, float countFraction) {
		//vertexArrayObject.drawIndirect();
		//vertexArrayObject.draw(currentFrameIndex * particlesPerFrame, maxParticlesDisplayed, true);
		//			shader.setUniform1f(UniformName.spriteSize, 0.04f);

		int vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, particleBuffer, GL_STREAM_DRAW);
		glVertexAttribPointer(ShaderLayout.in_Position.ordinal(), 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(ShaderLayout.in_Position.ordinal());
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		int start = (int) (maxParticlesDisplayed * startFraction);
		glDrawArrays(GL_POINTS, start, (int) (maxParticlesDisplayed * countFraction) - start);

		glDeleteBuffers(vboId);


	}


	public Vector3f getJumpThresholds() {
		if (jumpThresholds == null) {
			jumpThresholds = Vector3f.sub(getBoundingBoxMax(), getBoundingBoxMin(), null);
			jumpThresholds.scale(0.9f);
		}
		return jumpThresholds;
	}

	public void update(Matrix4f viewMatrix) {
		//		if (dataFrames.size() > uploadedFrames) {
		//			vertexArrayObject.appendPositionAndColorData(dataFrames.get(uploadedFrames), particleData.isColorDataSet() ? dataFramesColors.get(uploadedFrames) : null);
		//			uploadedFrames++;
		//		}

		uploadedFrames = dataFrames.size();

		if (!paused) {
			currentFrameIndexD += dataFps / SimpleObjectViewer.getFps();
			currentFrameIndex = (int) currentFrameIndexD;
		}


		float scaleStep = SimpleObjectViewer.getFrameTimeMs() / 1000.0f;


		if (InputManager.isKeyDown(GLFW.GLFW_KEY_KP_ADD)) {
			maxParticlesDisplayedF = maxParticlesDisplayedF * (1 + 2 * scaleStep) + 10 * scaleStep;
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_KP_SUBTRACT)) {
			maxParticlesDisplayedF = maxParticlesDisplayedF * (1 - 2 * scaleStep) - 10 * scaleStep;
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_N)) {
			scaleClippedX(1 + scaleStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_M)) {
			scaleClippedX(1 - scaleStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_E)) {
			scaleClipped(1 + scaleStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_Q)) {
			scaleClipped(1 - scaleStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_F)) {
			speedLineLengthF = speedLineLengthF * (1 + scaleStep) + 10 * scaleStep;
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_C)) {
			speedLineLengthF = speedLineLengthF * (1 - scaleStep) - 10 * scaleStep;
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_X)) {
			dataFps = MiscUtils.clamp(dataFps * (1 + scaleStep), 1, 1000);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_Z)) {
			dataFps = MiscUtils.clamp(dataFps * (1 - scaleStep), 1, 1000);
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

		if (InputManager.isKeyDown(GLFW.GLFW_KEY_F2) || InputManager.isKeyDown(GLFW.GLFW_KEY_F3)) {
			resetScalePos();
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_F4)) {
			resetScalePos();
			speedLineLengthF = 70;
			maxParticlesDisplayedF = 1;
		}
		if (InputManager.isLockedOnLeftMouse()) {
			addYaw(InputManager.pollMouseXd() * -mouseSensitivity);
			float yd = InputManager.pollMouseYd();
			addPitch((float) (Math.cos(MiscUtils.degreesToRadians(Scene.camera.getYaw() - getYaw())) * (yd * -mouseSensitivity)));
			addRoll((float) (Math.sin(MiscUtils.degreesToRadians(Scene.camera.getYaw() - getYaw())) * (yd * mouseSensitivity)));
		}


		// PROTECTION
		speedLineLength = (int) speedLineLengthF;
		maxParticlesDisplayed = (int) maxParticlesDisplayedF;
		maxParticlesDisplayedF = MiscUtils.clamp(maxParticlesDisplayedF, 1, particlesPerFrame);
		maxParticlesDisplayed = MiscUtils.clamp(maxParticlesDisplayed, 1, particlesPerFrame);
		speedLineLengthF = MiscUtils.clamp(speedLineLengthF, 0, uploadedFrames - 100);
		speedLineLength = MiscUtils.clamp(speedLineLength, 0, uploadedFrames - 100);
		if (currentFrameIndex >= uploadedFrames || currentFrameIndex < speedLineLength) {
			currentFrameIndexD = speedLineLength;
			currentFrameIndex = speedLineLength;
		}


		Matrix4f.mul(viewMatrix, getUpdatedModelMatrix(), modelViewMatrix);
		VertexSorter.generateSortingIndices(dataFrames.get(currentFrameIndex), maxParticlesDisplayed, modelViewMatrix);
		particleBuffer = VertexSorter.fillParticleBuffer(dataFrames, currentFrameIndex, maxParticlesDisplayed, particleBuffer);


		// HUD
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFps, paused ? 0 : dataFps);
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFrame, currentFrameIndex);
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFrameCount, dataFrames.size());
		HeadUpDisplay.putDebugValue(HudDebugKeys.numTailSegments, speedLineLength);
		HeadUpDisplay.putDebugValue(HudDebugKeys.numObjects, maxParticlesDisplayed * speedLineLength * 2 + maxParticlesDisplayed);
	}



	public void resetScalePos() {
		setPitch(0);
		setYaw(0);
		setRoll(0);
		setScale(1 / (getBoundingBoxMax().length() / 4));
		globalRadius = particleData.getGlobalRadius() / (getBoundingBoxMax().length() / 4);
	}

	public MmpldData getParticleData() {
		return particleData;
	}

	@Override
	public void update() {
	}

}
