package particleVisualization.rendering;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import java.nio.FloatBuffer;
import java.util.EnumMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import particleVisualization.enums.RenderMode;
import particleVisualization.enums.UniformName;
import particleVisualization.model.DrawableEntity;
import particleVisualization.util.ShaderUtils;


public class Shader {

	private static final FloatBuffer			matrix4x4Buffer	= BufferUtils.createFloatBuffer(16);

	private final int							shaderProgramId;
	private final EnumMap<UniformName, Integer>	perShaderUniformLocations;
	private RenderMode							renderMode;



	public Shader(Matrix4f projectionMatrix, String vertexShaderName, String fragmentShaderName) {
		shaderProgramId = ShaderUtils.buildShader(vertexShaderName, fragmentShaderName);
		perShaderUniformLocations = new EnumMap<UniformName, Integer>(UniformName.class); // blazing lookup speed ! O(1)
		for (UniformName uniform: UniformName.values()) {
			int loc = glGetUniformLocation(shaderProgramId, uniform.toString());
			if (loc != -1) {
				perShaderUniformLocations.put(uniform, loc);
			}
		}
		setup(projectionMatrix, 0);
	}

	public final void setup(Matrix4f projectionMatrix, int textureUnitId) {
		enable();
		setProjectionMatrix(projectionMatrix);
		setTextureUnitId(textureUnitId);
		setRenderMode(RenderMode.textured);
		setUniform1f(UniformName.spriteSize, 0.06f); //TODO cleanup
		setUniform2f(UniformName.screenSize, SimpleObjectViewer.windowWidth, SimpleObjectViewer.windowHeight);
		setUniform4f(UniformName.globalColor, .7f, .7f, .7f, 1);
		disable();
	}

	public final void draw(Matrix4f updatedViewMatrix, DrawableEntity... drawableEntities) {
		enable();
		setViewMatrix(updatedViewMatrix);
		for (DrawableEntity de: drawableEntities) {
			de.draw(this);
		}
		disable();
	}



	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		setMatrix(UniformName.projectionMatrix, projectionMatrix);
	}

	public void setViewMatrix(Matrix4f viewMatrix) {
		setMatrix(UniformName.viewMatrix, viewMatrix);
	}

	public void setModelMatrix(Matrix4f modelMatrix) {
		setMatrix(UniformName.modelMatrix, modelMatrix);
	}



	// ===================== openGL cmds =====================
	public void enable() {
		glUseProgram(shaderProgramId);
	}

	public void disable() {
		glUseProgram(0);
	}

	private void setUniform1f(UniformName uniform, float f) {
		if (perShaderUniformLocations.get(uniform) != null) {
			glUniform1f(perShaderUniformLocations.get(uniform), f);
		}
	}

	private void setUniform2f(UniformName uniform, float f1, float f2) {
		if (perShaderUniformLocations.get(uniform) != null) {
			glUniform2f(perShaderUniformLocations.get(uniform), f1, f2);
		}
	}

	private void setUniform4f(UniformName uniform, float f1, float f2, float f3, float f4) {
		if (perShaderUniformLocations.get(uniform) != null) {
			glUniform4f(perShaderUniformLocations.get(uniform), f1, f2, f3, f4);
		}
	}

	public void setRenderMode(RenderMode rm) {
		if (rm != renderMode && perShaderUniformLocations.get(UniformName.renderMode) != null) {
			renderMode = rm;
			glUniform1i(perShaderUniformLocations.get(UniformName.renderMode), renderMode.ordinal());
		}
	}

	public void setTextureUnitId(int textureUnitId) {
		if (perShaderUniformLocations.get(UniformName.textureUnitId) != null) {
			glActiveTexture(textureUnitId + GL_TEXTURE0);
			glUniform1i(perShaderUniformLocations.get(UniformName.textureUnitId), textureUnitId);
		}
	}

	public void setMatrix(UniformName uniformMatrixName, Matrix4f matrix) {
		if (perShaderUniformLocations.get(uniformMatrixName) != null) {
			matrix.store(matrix4x4Buffer);
			matrix4x4Buffer.flip();
			glUniformMatrix4(perShaderUniformLocations.get(uniformMatrixName), false, matrix4x4Buffer);
		}
	}

	public void destroy() {
		glDeleteProgram(shaderProgramId);
	}



}
