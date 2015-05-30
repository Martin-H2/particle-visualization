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
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import particleVisualization.enums.ShaderLayout;
import particleVisualization.util.MiscUtils;

public class VertexArrayObject {

	private int				vertexCountTotal;
	private final int		vaoId;
	private final int		vboId;
	private int				iboId;
	private int				tcboId	= -1;
	private final int		drawMode;

	private final boolean	indexedMode;
	private final boolean	streamingMode;
	private long			positionBufferByteOffset;

	public VertexArrayObject(float[] positions, byte[] indices, float[] textureCoordinates, int drawMode) {
		this(positions, indices, textureCoordinates, drawMode, -1);
	}

	public VertexArrayObject(float[] positions, byte[] indices, float[] textureCoordinates, int drawMode, int verticesTargetCount) {
		this.drawMode = drawMode;
		indexedMode = indices != null;
		streamingMode = verticesTargetCount != -1;

		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		if (streamingMode) {
			glBufferData(GL_ARRAY_BUFFER, verticesTargetCount * 3 * 4, GL_STREAM_DRAW);
			glBufferSubData(GL_ARRAY_BUFFER, 0, MiscUtils.createFloatBuffer(positions));
			positionBufferByteOffset = positions.length * 4;
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

	public void bind() {
		glBindVertexArray(vaoId);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
	}

	public void unbind() {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	public void draw() {
		draw(0, vertexCountTotal);
	}

	public void draw(int vertexOffset, int vertexCount) {
		bind();
		if (indexedMode) {
			glDrawElements(drawMode, vertexCount, GL_UNSIGNED_BYTE, vertexOffset);
		}
		else {
			glDrawArrays(drawMode, vertexOffset, vertexCount);
		}
		unbind();
	}

	public void appendPositionData(float[] newPositions) {
		glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId); //TODO mit draw kombinieren ?
		glBufferSubData(GL_ARRAY_BUFFER, positionBufferByteOffset, MiscUtils.createFloatBuffer(newPositions));
		glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		positionBufferByteOffset += newPositions.length * 4;
		vertexCountTotal += newPositions.length / 3;
	}

	public void destroy() {
		GL15.glDeleteBuffers(tcboId);
		GL15.glDeleteBuffers(vboId);
		GL15.glDeleteBuffers(iboId);
		GL30.glDeleteVertexArrays(vaoId);
	}



}
