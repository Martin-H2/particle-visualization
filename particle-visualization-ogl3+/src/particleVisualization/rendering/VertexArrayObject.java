package particleVisualization.rendering;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import org.lwjgl.opengl.*;
import particleVisualization.enums.ShaderLayout;
import particleVisualization.util.MiscUtils;

public class VertexArrayObject {

	private static Random	random	= new Random();

	private int				vertexCountTotal;
	private final int		vaoId;
	private final int		pVboId;
	private int				cVboId;
	private int				indirectBufferId;
	private int				iboId;
	private int				tcboId	= -1;
	private final int		drawMode;

	private final boolean	indexedMode;
	private final boolean	streamingMode;
	private long			positionBufferByteWriteOffset;
	private IntBuffer		indirectBuffer;


	public VertexArrayObject(float[] initialPositions, float[] initialColors, int primitiveMode, int verticesTargetCount) {
		this(initialPositions, initialColors, null, null, primitiveMode, verticesTargetCount);
	}

	public VertexArrayObject(float[] positions, byte[] indices, float[] textureCoordinates, int primitiveMode) {
		this(positions, null, indices, textureCoordinates, primitiveMode, -1);
	}

	public VertexArrayObject(float[] positions, float[] initialColors, byte[] indices, float[] textureCoordinates, int primitiveMode, int verticesTargetCount) {
		drawMode = primitiveMode;
		indexedMode = indices != null;
		streamingMode = verticesTargetCount != -1;


		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);


		pVboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, pVboId);
		if (streamingMode) {
			glBufferData(GL_ARRAY_BUFFER, verticesTargetCount * 3 * 4, GL_STREAM_DRAW);
			glBufferSubData(GL_ARRAY_BUFFER, 0, MiscUtils.createFloatBuffer(positions));
			positionBufferByteWriteOffset = positions.length * 4;
		}
		else {
			glBufferData(GL_ARRAY_BUFFER, MiscUtils.createFloatBuffer(positions), GL_STATIC_DRAW);
		}
		glVertexAttribPointer(ShaderLayout.in_Position.ordinal(), 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(ShaderLayout.in_Position.ordinal());

		if (textureCoordinates != null) {
			tcboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, tcboId);
			glBufferData(GL_ARRAY_BUFFER, MiscUtils.createFloatBuffer(textureCoordinates), GL_STATIC_DRAW);
			glVertexAttribPointer(ShaderLayout.in_TextureCoord.ordinal(), 2, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(ShaderLayout.in_TextureCoord.ordinal());
		}

		if (initialColors != null) {
			cVboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, cVboId);
			if (streamingMode) {
				//System.out.println("color data in streaming mode: " + initialColors.length);
				glBufferData(GL_ARRAY_BUFFER, verticesTargetCount * 4, GL_STREAM_DRAW);
				glBufferSubData(GL_ARRAY_BUFFER, 0, MiscUtils.createFloatBuffer(initialColors));
			}
			else {
				glBufferData(GL_ARRAY_BUFFER, MiscUtils.createFloatBuffer(initialColors), GL_STATIC_DRAW);
			}
			glVertexAttribPointer(ShaderLayout.in_Color.ordinal(), 4, GL_UNSIGNED_BYTE, true, 4, 0); // "hack" - using 1 FLOAT as 4 UNSIGNED_BYTE (rgba)
			glEnableVertexAttribArray(ShaderLayout.in_Color.ordinal());
		}

		if (indexedMode) {
			vertexCountTotal = indices.length;
			iboId = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, MiscUtils.createByteBuffer(indices), GL_STATIC_DRAW);
		}
		else {
			vertexCountTotal = positions.length / 3;
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}


	public void setupIndirectBuffer() { //TODO content as param
		indirectBuffer = MiscUtils.createIntBuffer(new int[] {
			3, 1, 0, 0, // vertexCount, instanceCount, firstVertex, baseInstance
			3, 1, 3, 0,
			3, 1, 6, 0,
		});
		indirectBufferId = glGenBuffers();
		glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBufferId);
		glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBuffer, GL_STREAM_DRAW);
	}

	public void bind() {
		glBindVertexArray(vaoId);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
	}

	public void unbind() {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	public void draw() {
		draw(0, vertexCountTotal, false);
	}

	public void draw(int vertexOffset, int vertexDrawCount, boolean wrapIndicesAround) {
		if (vertexOffset >= vertexCountTotal) {
			System.err.println("ERROR @VOA.draw: vertexOffset out of bounds");
		}
		bind();
		if (indexedMode) {
			glDrawElements(drawMode, vertexDrawCount, GL_UNSIGNED_BYTE, vertexOffset);
		}
		else {
			if (wrapIndicesAround && vertexOffset + vertexDrawCount > vertexCountTotal) {
				int overflow = vertexOffset + vertexDrawCount - vertexCountTotal;
				glDrawArrays(drawMode, vertexOffset, vertexDrawCount - overflow);
				glDrawArrays(drawMode, 0, overflow); //wrapIndicesAround!
			}
			else {
				glDrawArrays(drawMode, vertexOffset, vertexDrawCount);
			}
		}
		unbind();
	}

	public void drawIndirect() {
		glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBufferId);
		//GL43.glMultiDrawArraysIndirect(GL11.GL_LINE_STRIP, indirectBuffer, 3, 0);
		GL43.glMultiDrawArraysIndirect(GL11.GL_LINE_STRIP, 0, 3, 0);
	}

	public void appendPositionAndColorData(float[] newPositions, float[] newColors) {
		//System.out.println("appendPositionAndColorData: " + newPositions + newColors);
		glBindBuffer(GL_ARRAY_BUFFER, pVboId);
		glBufferSubData(GL_ARRAY_BUFFER, positionBufferByteWriteOffset, MiscUtils.createFloatBuffer(newPositions));
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		if (newColors != null) {
			glBindBuffer(GL_ARRAY_BUFFER, cVboId);
			glBufferSubData(GL_ARRAY_BUFFER, positionBufferByteWriteOffset / 3, MiscUtils.createFloatBuffer(newColors));
			glBindBuffer(GL_ARRAY_BUFFER, 0);
		}
		positionBufferByteWriteOffset += newPositions.length * 4;
		vertexCountTotal += newPositions.length / 3;
	}

	public void destroy() {
		GL15.glDeleteBuffers(tcboId);
		GL15.glDeleteBuffers(pVboId);
		GL15.glDeleteBuffers(iboId);
		GL30.glDeleteVertexArrays(vaoId);
	}

	public void testVboMapping(int numberOfFloatsBound) {
		glBindBuffer(GL_ARRAY_BUFFER, pVboId);
		FloatBuffer fb = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER, 0, numberOfFloatsBound * 4, GL30.GL_MAP_WRITE_BIT | GL30.GL_MAP_UNSYNCHRONIZED_BIT, null).asFloatBuffer();
		//System.out.println("=== p.pos ===" + fb.toString());
		//fb.position(0);
		for (int i = 0; i < fb.capacity(); i++) {
			float f = fb.get(i);
			//System.out.println(f);
			fb.put(i, (float) (f + random.nextGaussian() * 0.001));
		}
		glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}



}
