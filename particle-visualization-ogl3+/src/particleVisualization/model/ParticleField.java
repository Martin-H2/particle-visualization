package particleVisualization.model;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.vector.Vector4f;

import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.enums.RenderMode;
import particleVisualization.enums.ShaderLayout;
import particleVisualization.enums.UniformName;
import particleVisualization.rendering.*;
import particleVisualization.util.MiscUtils;

public class ParticleField extends DrawableEntity {

	private final List<float[]>	dataFrames;
	private int					currentFrameIndex		= 0;
	private double				currentFrameIndexD		= 0;

	private final Vector4f		globalRgba;
	private int					maxParticlesDisplayed	= 3000;
	private final int			particlesPerFrame;
	private int					speedLineLength			= 1;
	private float				tailLength				= 0;
	private boolean				paused					= true;
	private float				dataFps					= SimpleObjectViewer.refreshRate;
	private int					uploadedFrames;

	private final float			mouseSensitivity		= 0.15f;

	private static Random		random					= new Random();
	private FloatBuffer			fb;


	public ParticleField(MmpldData particleData, Texture spriteTexture) {
		super(spriteTexture, particleData.getDataFrames().get(0),
				particleData.getNumberOfDataFrames() * particleData.getParticlesPerFrame(),
				GL11.GL_POINTS, RenderMode.pointSprite);
		uploadedFrames = 1;
		dataFrames = particleData.getDataFrames();
		globalRgba = particleData.getGlobalRgba();
		particlesPerFrame = particleData.getParticlesPerFrame();
		setBoundingBoxMin(particleData.getBoxMin());
		setBoundingBoxMax(particleData.getBoxMax());
		drawBoundingBox(true);
		GL11.glEnable(GL20.GL_POINT_SPRITE);
		GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
		//vertexArrayObject.setupIndirectBuffer();
		//fb = MiscUtils.createFloatBuffer(dataFrames.get(0));
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
		shader.setUniform1f(UniformName.spriteSize, 0.04f);
	}

	@Override
	protected void drawVao(Shader shader) {
		//vertexArrayObject.drawIndirect();
		vertexArrayObject.draw((currentFrameIndex + speedLineLength) * particlesPerFrame, maxParticlesDisplayed, true);

		drawSpeedLines(shader);
	}

	private void drawSpeedLines(Shader shader) {
		shader.setRenderMode(RenderMode.globalColored);

		//System.out.println("FRAME_LAYOUT: " + MiscUtils.vertexLayoutToString(dataFrames.get(0), 3, 3));
		fb = MiscUtils.frameLayoutToSpeedlineLayout(dataFrames, currentFrameIndex, speedLineLength, fb, maxParticlesDisplayed);
		//System.out.println("SLINE_LAYOUT: " + MiscUtils.vertexLayoutToString(fb, 3, 3));

		int vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, fb, GL_STREAM_DRAW); //1260 - 1280 fps
		glVertexAttribPointer(ShaderLayout.in_Position.ordinal(), 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(ShaderLayout.in_Position.ordinal());

		//		GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
		//		GL31.glPrimitiveRestartIndex(0);
//		glDrawArrays(GL11.GL_LINES, 0, fb.limit());
		IntBuffer startingIndicesArray = MiscUtils.createIntBuffer(new int[] {0});
		IntBuffer numberOfverticesArray = MiscUtils.createIntBuffer(new int[] {speedLineLength-1});
		GL14.glMultiDrawArrays(GL11.GL_LINE_STRIP, startingIndicesArray, numberOfverticesArray);

		//draw mini points
		//		shader.setUniform1f(UniformName.spriteSize, 0.01f);
		//		shader.setRenderMode(RenderMode.pointSprite);
		//		glDrawArrays(GL11.GL_POINTS, 0, fb.limit());
		//		GL11.glDisable(GL31.GL_PRIMITIVE_RESTART);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboId);
	}

	public void increaseMaxParticles(int maxParticlesInc) {
		maxParticlesDisplayed += maxParticlesInc;
		if (maxParticlesDisplayed <= 0) {
			maxParticlesDisplayed = 1;
		}
		if (maxParticlesDisplayed > particlesPerFrame) {
			maxParticlesDisplayed = particlesPerFrame;
		}
	}



	@Override
	public void update() {
		//TODO send new dataFrames to GPU
		if (dataFrames.size() > uploadedFrames) {
			vertexArrayObject.appendPositionData(dataFrames.get(uploadedFrames)); //FIXME can be null sometimes ...
			uploadedFrames++;
		}

		if (!paused) {
			currentFrameIndexD = (currentFrameIndexD + dataFps / SimpleObjectViewer.getFps()) % (uploadedFrames - speedLineLength);
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
			tailLength = MiscUtils.clip(tailLength * (1 + scaleStep) + 10 * scaleStep, 0, uploadedFrames - 2);
			speedLineLength = (int) tailLength + 1;
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_C)) {
			tailLength = MiscUtils.clip(tailLength * (1 - scaleStep) - 10 * scaleStep, 0, uploadedFrames - 2);
			speedLineLength = (int) tailLength + 1;
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
		HeadUpDisplay.putDebugValue(HudDebugKeys.numTailSegments, speedLineLength - 1);
		HeadUpDisplay.putDebugValue(HudDebugKeys.numObjects, particlesPerFrame * speedLineLength);
	}

}
