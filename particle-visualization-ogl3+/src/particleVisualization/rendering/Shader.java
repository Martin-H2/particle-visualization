package particleVisualization.rendering;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

import particleVisualization.model.DrawableEntity;
import particleVisualization.util.ShaderLayout;
import particleVisualization.util.ShaderUtils;


public class Shader {

	private static final FloatBuffer	matrix4x4Buffer	= BufferUtils.createFloatBuffer(16);

	private final int					shaderProgramId;

	private final int					projectionMatrixLocation;
	private final int					viewMatrixLocation;
	private final int					modelMatrixLocation;
	private final int					textureUnitIdLocation;


	public Shader(Matrix4f projectionMatrix, String vertexShaderName, String fragmentShaderName) {
		shaderProgramId = ShaderUtils.buildShader(vertexShaderName, fragmentShaderName);
		projectionMatrixLocation = glGetUniformLocation(shaderProgramId, ShaderLayout.UNIFORM_PROJECTION_MATRIX_NAME);
		viewMatrixLocation = glGetUniformLocation(shaderProgramId, ShaderLayout.UNIFORM_VIEW_MATRIX_NAME);
		modelMatrixLocation = glGetUniformLocation(shaderProgramId, ShaderLayout.UNIFORM_MODEL_MATRIX_NAME);
		textureUnitIdLocation = glGetUniformLocation(shaderProgramId, ShaderLayout.UNIFORM_TEXTURE_UNIT_NAME);
		setup(projectionMatrix, 0);
	}

	public void setup(Matrix4f projectionMatrix, int textureUnitId) {
		enable();
		setProjectionMatrix(projectionMatrix);
		setTextureUnitId(textureUnitId);

		glUniform2f(glGetUniformLocation(shaderProgramId, "screenSize"), 1280, 720); //FIXME cleanup
		glUniform1f(glGetUniformLocation(shaderProgramId, "spriteSize"), 0.06f);
		glUniform4f(glGetUniformLocation(shaderProgramId, "tintingColor"), .5f,.5f,1,1);



		disable();
	}

	public void draw(Matrix4f updatedViewMatrix, DrawableEntity... drawableEntities) {
		enable();
		setViewMatrix(updatedViewMatrix);
		for (DrawableEntity de: drawableEntities) {
			de.draw(this);
		}
		disable();
	}



	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		setMatrix(projectionMatrixLocation, projectionMatrix);
	}

	public void setViewMatrix(Matrix4f viewMatrix) {
		setMatrix(viewMatrixLocation, viewMatrix);
	}

	public void setModelMatrix(Matrix4f modelMatrix) {
		setMatrix(modelMatrixLocation, modelMatrix);
	}

	// ======= openGL cmds =======
	public void enable() {
		glUseProgram(shaderProgramId);
	}

	public void disable() {
		glUseProgram(0);
	}

	public void setTextureUnitId(int textureUnitId) {
		glActiveTexture(textureUnitId + GL_TEXTURE0);
		glUniform1i(textureUnitIdLocation, textureUnitId);
	}

	public void setMatrix(int matrixLocation, Matrix4f matrix) {
		matrix.store(matrix4x4Buffer);
		matrix4x4Buffer.flip();
		glUniformMatrix4(matrixLocation, false, matrix4x4Buffer);
	}

	public void destroy() {
		glDeleteProgram(shaderProgramId);
	}



}
