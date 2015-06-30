package particleVisualization.model;

import org.lwjgl.opengl.GL11;
import particleVisualization.rendering.Shader;
import particleVisualization.rendering.Texture;

public class CubeTris extends DrawableEntity {

	private static final int		primitiveMode	= GL11.GL_TRIANGLES;

	private static final float[]	vertices		= {
													-0.5f, 0.5f, -0.5f,
													-0.5f, -0.5f, -0.5f,
													0.5f, -0.5f, -0.5f,
													0.5f, 0.5f, -0.5f,

													-0.5f, 0.5f, 0.5f,
													-0.5f, -0.5f, 0.5f,
													0.5f, -0.5f, 0.5f,
													0.5f, 0.5f, 0.5f,

													0.5f, 0.5f, -0.5f,
													0.5f, -0.5f, -0.5f,
													0.5f, -0.5f, 0.5f,
													0.5f, 0.5f, 0.5f,

													-0.5f, 0.5f, -0.5f,
													-0.5f, -0.5f, -0.5f,
													-0.5f, -0.5f, 0.5f,
													-0.5f, 0.5f, 0.5f,

													-0.5f, 0.5f, 0.5f,
													-0.5f, 0.5f, -0.5f,
													0.5f, 0.5f, -0.5f,
													0.5f, 0.5f, 0.5f,

													-0.5f, -0.5f, 0.5f,
													-0.5f, -0.5f, -0.5f,
													0.5f, -0.5f, -0.5f,
													0.5f, -0.5f, 0.5f
													};

	private static final byte[]		indices			= {
													0, 1, 3,
													3, 1, 2,
													4, 5, 7,
													7, 5, 6,
													8, 9, 11,
													11, 9, 10,
													12, 13, 15,
													15, 13, 14,
													16, 17, 19,
													19, 17, 18,
													20, 21, 23,
													23, 21, 22
													};

	private static final float[]	texCoords		= {
													0, 0,
													0, 1,
													1, 1,
													1, 0,
													0, 0,
													0, 1,
													1, 1,
													1, 0,
													0, 0,
													0, 1,
													1, 1,
													1, 0,
													0, 0,
													0, 1,
													1, 1,
													1, 0,
													0, 0,
													0, 1,
													1, 1,
													1, 0,
													0, 0,
													0, 1,
													1, 1,
													1, 0
													};

	public CubeTris(Texture texture) {
		super(texture, vertices, indices, texCoords, primitiveMode);
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
	}

	@Override
	protected void drawVao(Shader shader) {
		vertexArrayObject.draw();
	}

	@Override
	public void update() {
	}



}
