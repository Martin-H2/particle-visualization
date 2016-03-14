package particleVisualization.model;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL32;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.enums.RenderMode;
import particleVisualization.enums.ShaderLayout;
import particleVisualization.enums.UniformName;
import particleVisualization.rendering.HeadUpDisplay;
import particleVisualization.rendering.Scene;
import particleVisualization.rendering.Shader;
import particleVisualization.rendering.Texture;
import particleVisualization.util.MiscUtils;
import particleVisualization.util.VertexSorter;


public class ParticleFieldSpeedLines extends DrawableEntity {

	private final FloatBuffer	staticSpeedLineOverlayBuffer;
	private final FloatBuffer	staticSpeedLineOverlayLineStripOffsets;
	private FloatBuffer			speedLineBuffer;
	private FloatBuffer			lineStripOffsets;
	private final IntBuffer		startingIndicesList;
	private final IntBuffer		numberOfverticesList;
	private final ParticleField	particleField;
	private final boolean		jumpCompensation			= true;
	private final float			filterKernel				= 0.5f;
	private float				speedlineTransparency		= 0.3f;
	private float				textureFact					= 0.5f;
	private float				textureYScale				= 1.0f;
	private final boolean		drawStaticClusterOverlay	= false;
	int							vboId						= -1, oVboId = -1;


	public ParticleFieldSpeedLines(ParticleField particleField, Texture texture) {
		super(RenderMode.globalColored, texture);
		this.particleField = particleField;
		startingIndicesList = BufferUtils.createIntBuffer(particleField.particlesPerFrame * 2);
		numberOfverticesList = BufferUtils.createIntBuffer(particleField.particlesPerFrame * 2);
		linkModelMatrix(particleField.getUpdatedModelMatrix());

		//bbox: 400 / 400 / 200
		int numSegments = 50;
		double radius = 35;
		staticSpeedLineOverlayBuffer = BufferUtils.createFloatBuffer(numSegments * 3);
		staticSpeedLineOverlayLineStripOffsets = BufferUtils.createFloatBuffer(numSegments);

		//		for (double segmentAngleRad = 0; segmentAngleRad < 2 * Math.PI; segmentAngleRad += 2 * Math.PI / numSegments) {
		//			//System.out.println("\nvertex: [ " + (Math.sin(segmentAngleRad) * radius + 200) + " / " + (Math.cos(segmentAngleRad) * radius + 200) + " / 100 ]");
		//			float index = 1f - (float) (Math.sin(segmentAngleRad / 2) * 0.9);
		//			//System.out.println("index: " + index);
		//			staticSpeedLineOverlayBuffer.put((float) (Math.sin(segmentAngleRad) * radius + 205));
		//			staticSpeedLineOverlayBuffer.put((float) (Math.cos(segmentAngleRad) * radius + 160));
		//			staticSpeedLineOverlayBuffer.put(100);
		//			staticSpeedLineOverlayLineStripOffsets.put(index);
		//		}
		for (float i = 0; i < 400; i += 400 / numSegments) {
			//System.out.println("\nvertex: [ " + (Math.sin(segmentAngleRad) * radius + 200) + " / " + (Math.cos(segmentAngleRad) * radius + 200) + " / 100 ]");
			float index = i / 400f;
			//System.out.println("index: " + index);
			staticSpeedLineOverlayBuffer.put(i);
			staticSpeedLineOverlayBuffer.put(i);
			staticSpeedLineOverlayBuffer.put(100);
			staticSpeedLineOverlayLineStripOffsets.put(index);
		}

		staticSpeedLineOverlayBuffer.flip();
		staticSpeedLineOverlayLineStripOffsets.flip();


	}

	@Override
	public void update() {
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_3) && InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PAGE_UP)) {
			textureYScale = MiscUtils.clamp(textureYScale + 0.1f, 0.1f, 3.0f);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_3) && InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PAGE_DOWN)) {
			textureYScale = MiscUtils.clamp(textureYScale - 0.1f, 0.1f, 3.0f);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_2) && InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PAGE_UP)) {
			speedlineTransparency = MiscUtils.clamp(speedlineTransparency + 0.1f, 0, 1);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_2) && InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PAGE_DOWN)) {
			speedlineTransparency = MiscUtils.clamp(speedlineTransparency - 0.1f, 0, 1);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_1) && InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PAGE_UP)) {
			textureFact = MiscUtils.clamp(textureFact + 0.1f, 0, 1);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_1) && InputManager.isKeyDownEvent(GLFW.GLFW_KEY_PAGE_DOWN)) {
			textureFact = MiscUtils.clamp(textureFact - 0.1f, 0, 1);
		}
		HeadUpDisplay.putDebugValue(HudDebugKeys.speedlineTransparency, speedlineTransparency);
		HeadUpDisplay.putDebugValue(HudDebugKeys.textureFact, textureFact);
		HeadUpDisplay.putDebugValue(HudDebugKeys.textureYScale, textureYScale);

		int offsetCount = particleField.particlesPerFrame * (particleField.speedLineLength + 1) + particleField.particlesPerFrame;
		//		System.out.println("FRAME_LAYOUT: " + MiscUtils.vertexLayoutToString(dataFrames.get(0), 3, 3));
		if (!ParticleField.persistentMode) {
			if (lineStripOffsets == null || lineStripOffsets.capacity() < offsetCount) {
				lineStripOffsets = BufferUtils.createFloatBuffer(offsetCount * 4);
				System.out.println("#PFS.update.createFloatBuffer !");
			}
			speedLineBuffer = VertexSorter.frameLayoutToSpeedlineLayout(particleField.dataFrames, particleField.currentFrameIndex,
					particleField.speedLineLength + 1, 0, particleField.maxParticlesDisplayed, speedLineBuffer, lineStripOffsets, startingIndicesList,
					numberOfverticesList, jumpCompensation, filterKernel, particleField.getJumpThresholds());
			//		System.out.println("SLINE_LAYOUT: " + MiscUtils.vertexLayoutToString(speedLineBuffer, 3, 3) + "  speedLineLength:" + speedLineLength);
			//		System.out.println("startingIndicesArray: " + startingIndicesList.toString());
		}
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
		shader.setUniform1f(UniformName.spriteSize, particleField.globalRadius * 2f);
		shader.setUniform4f(UniformName.fogColor, Scene.BG_COLOR);
		shader.setUniform1f(UniformName.fogDensity, Scene.FOG_DENSITY);
		shader.setUniform1f(UniformName.speedlineTransparency, speedlineTransparency);
		shader.setUniform1f(UniformName.textureFact, textureFact);
		shader.setUniform1f(UniformName.textureYScale, textureYScale);
	}

	@Override
	protected void drawVao(Shader shader, float startFraction, float countFraction) {
		if (particleField.speedLineLength > 0) {
			boolean recreateBuffers = vboId == -1 || oVboId == -1 || !ParticleField.persistentMode;

			if (recreateBuffers) {
				glDeleteBuffers(vboId);
				vboId = glGenBuffers();
			}
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			if (recreateBuffers) {
				glBufferData(GL_ARRAY_BUFFER, speedLineBuffer, GL_STATIC_DRAW);
			}
			glVertexAttribPointer(ShaderLayout.in_Position.ordinal(), 3, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(ShaderLayout.in_Position.ordinal());
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			if (recreateBuffers) {
				glDeleteBuffers(oVboId);
				oVboId = glGenBuffers();
			}
			glBindBuffer(GL_ARRAY_BUFFER, oVboId);
			if (recreateBuffers) {
				glBufferData(GL_ARRAY_BUFFER, lineStripOffsets, GL_STATIC_DRAW);
			}
			glVertexAttribPointer(ShaderLayout.in_Offset.ordinal(), 1, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(ShaderLayout.in_Offset.ordinal());
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			startingIndicesList.position((int) (particleField.maxParticlesDisplayed * startFraction));
			numberOfverticesList.position((int) (particleField.maxParticlesDisplayed * startFraction));

			startingIndicesList.limit((int) (particleField.maxParticlesDisplayed * countFraction));
			numberOfverticesList.limit((int) (particleField.maxParticlesDisplayed * countFraction));

			Scene.gpuMem += 4 * (speedLineBuffer.capacity() + lineStripOffsets.capacity() + startingIndicesList.remaining() + numberOfverticesList.remaining());

			GL14.glMultiDrawArrays(GL32.GL_LINE_STRIP_ADJACENCY, startingIndicesList, numberOfverticesList);

		}


		if (drawStaticClusterOverlay) {

			shader.setUniform1f(UniformName.spriteSize, 0.1f);

			vboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, staticSpeedLineOverlayBuffer, GL_STREAM_DRAW);
			glVertexAttribPointer(ShaderLayout.in_Position.ordinal(), 3, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(ShaderLayout.in_Position.ordinal());
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			oVboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, oVboId);
			glBufferData(GL_ARRAY_BUFFER, staticSpeedLineOverlayLineStripOffsets, GL_STREAM_DRAW);
			glVertexAttribPointer(ShaderLayout.in_Offset.ordinal(), 1, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(ShaderLayout.in_Offset.ordinal());
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			GL11.glDrawArrays(GL32.GL_LINE_STRIP_ADJACENCY, 0, staticSpeedLineOverlayLineStripOffsets.capacity());

			glDeleteBuffers(vboId);
			glDeleteBuffers(oVboId);
		}


	}



}
