package particleVisualization.model;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL32;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.enums.RenderMode;
import particleVisualization.enums.ShaderLayout;
import particleVisualization.enums.UniformName;
import particleVisualization.rendering.HeadUpDisplay;
import particleVisualization.rendering.Shader;
import particleVisualization.util.MiscUtils;


public class ParticleFieldSpeedLines extends DrawableEntity {

	private FloatBuffer			speedLineBuffer;
	private FloatBuffer			lineStripOffsets;
	private final IntBuffer		startingIndicesList;
	private final IntBuffer		numberOfverticesList;
	private final ParticleField	particleField;
	private boolean				jumpCompensation	= true;
	private float				filterKernel		= 0.5f;

	public ParticleFieldSpeedLines(ParticleField particleField) {
		super(RenderMode.globalColored);
		this.particleField = particleField;
		startingIndicesList = BufferUtils.createIntBuffer(particleField.particlesPerFrame * 2);
		numberOfverticesList = BufferUtils.createIntBuffer(particleField.particlesPerFrame * 2);
		linkModelMatrix(particleField.getUpdatedModelMatrix());

		if (particleField.getParticleData().getFileName().startsWith("dreikugeln")) { //TODO introduce clever autoselection
			jumpCompensation = false;
		}
	}

	@Override
	public void update() {
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PAGE_UP)) {
			filterKernel = MiscUtils.clip(filterKernel + 0.1f, 0, 1);
		}
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PAGE_DOWN)) {
			filterKernel = MiscUtils.clip(filterKernel - 0.1f, 0, 1);
		}
		HeadUpDisplay.putDebugValue(HudDebugKeys.filterKernel, filterKernel);

		int offsetCount = particleField.particlesPerFrame * (particleField.speedLineLength + 1) + particleField.particlesPerFrame;
		//		System.out.println("FRAME_LAYOUT: " + MiscUtils.vertexLayoutToString(dataFrames.get(0), 3, 3));
		if (lineStripOffsets == null || lineStripOffsets.capacity() != offsetCount) {
			lineStripOffsets = BufferUtils.createFloatBuffer(offsetCount);
		}
		speedLineBuffer = MiscUtils.frameLayoutToSpeedlineLayout(particleField.dataFrames, particleField.currentFrameIndex,
				particleField.speedLineLength + 1, 1, particleField.maxParticlesDisplayed, speedLineBuffer, lineStripOffsets, startingIndicesList,
				numberOfverticesList, jumpCompensation, filterKernel);
		//		System.out.println("SLINE_LAYOUT: " + MiscUtils.vertexLayoutToString(speedLineBuffer, 3, 3) + "  speedLineLength:" + speedLineLength);
		//		System.out.println("startingIndicesArray: " + startingIndicesList.toString());
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
		shader.setUniform1f(UniformName.spriteSize, particleField.globalRadius * 0.1f);
	}

	@Override
	protected void drawVao(Shader shader) {
		if (particleField.speedLineLength > 0) {

			int vboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, speedLineBuffer, GL_STREAM_DRAW);
			glVertexAttribPointer(ShaderLayout.in_Position.ordinal(), 3, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(ShaderLayout.in_Position.ordinal());
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			int oVboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, oVboId);
			glBufferData(GL_ARRAY_BUFFER, lineStripOffsets, GL_STREAM_DRAW);
			glVertexAttribPointer(ShaderLayout.in_Offset.ordinal(), 1, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(ShaderLayout.in_Offset.ordinal());
			glBindBuffer(GL_ARRAY_BUFFER, 0);


			GL14.glMultiDrawArrays(GL32.GL_LINE_STRIP_ADJACENCY, startingIndicesList, numberOfverticesList);

			glDeleteBuffers(vboId);
			glDeleteBuffers(oVboId);
		}
	}



}
