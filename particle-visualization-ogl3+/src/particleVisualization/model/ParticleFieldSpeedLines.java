package particleVisualization.model;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL14;
import particleVisualization.enums.RenderMode;
import particleVisualization.enums.ShaderLayout;
import particleVisualization.enums.UniformName;
import particleVisualization.rendering.Shader;
import particleVisualization.util.MiscUtils;


public class ParticleFieldSpeedLines extends DrawableEntity {

	private FloatBuffer			speedLineBuffer;
	private final IntBuffer		startingIndicesList;
	private final IntBuffer		numberOfverticesList;
	private final ParticleField	particleField;
	private boolean				jumpCompensation	= true;

	public ParticleFieldSpeedLines(ParticleField particleField) {
		super(RenderMode.globalColored);
		this.particleField = particleField;
		//fb = MiscUtils.createFloatBuffer(dataFrames.get(0));
		startingIndicesList = BufferUtils.createIntBuffer(particleField.particlesPerFrame * 2);
		numberOfverticesList = BufferUtils.createIntBuffer(particleField.particlesPerFrame * 2);
		linkModelMatrix(particleField.getUpdatedModelMatrix());

		if (particleField.getParticleData().getFileName().startsWith("dreikugeln")) { //TODO introduce clever autoselection
			jumpCompensation = false;
		}
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
		shader.setUniform1f(UniformName.spriteSize, particleField.globalRadius);
	}

	@Override
	protected void drawVao(Shader shader) {
		if (particleField.speedLineLength > 0) {
			drawSpeedLines(shader);
		}
	}

	private void drawSpeedLines(Shader shader) {
		//shader.setRenderMode(RenderMode.globalColored);

		//		System.out.println("FRAME_LAYOUT: " + MiscUtils.vertexLayoutToString(dataFrames.get(0), 3, 3));
		speedLineBuffer = MiscUtils.frameLayoutToSpeedlineLayout(particleField.dataFrames, particleField.currentFrameIndex, particleField.speedLineLength + 1,
				1, particleField.maxParticlesDisplayed, speedLineBuffer, startingIndicesList, numberOfverticesList, jumpCompensation);
		//		System.out.println("SLINE_LAYOUT: " + MiscUtils.vertexLayoutToString(speedLineBuffer, 3, 3) + "  speedLineLength:" + speedLineLength);
		//		System.out.println("startingIndicesArray: " + startingIndicesList.toString());

		int vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, speedLineBuffer, GL_STREAM_DRAW); //1260 - 1280 fps
		glVertexAttribPointer(ShaderLayout.in_Position.ordinal(), 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(ShaderLayout.in_Position.ordinal());

		//		glEnable(GL31.GL_PRIMITIVE_RESTART);
		//		GL31.glPrimitiveRestartIndex(0);
		//glDrawArrays(GL_LINES, 0, speedLineBuffer.limit());
		GL14.glMultiDrawArrays(GL_LINE_STRIP, startingIndicesList, numberOfverticesList);

		//		if (particleField.drawMiniPoints) {
		//			shader.setUniform1f(UniformName.spriteSize, 0.01f);
		//			shader.setRenderMode(RenderMode.pointSprite);
		//			glDrawArrays(GL_POINTS, 0, speedLineBuffer.limit());
		//			glDisable(GL31.GL_PRIMITIVE_RESTART);
		//		}

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboId);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

}
