package particleVisualization.model;

import org.lwjgl.opengl.GL11;
import particleVisualization.rendering.Shader;
import particleVisualization.rendering.Texture;

public class CubeQuads extends DrawableEntity {


	private static final int		primitiveMode	= GL11.GL_QUADS;

	private static final float[]	vertices		= {
													-0.5f, -0.5f, 0.5f,
													0.5f, -0.5f, 0.5f,
													0.5f, 0.5f, 0.5f,
													-0.5f, 0.5f, 0.5f,

													-0.5f, 0.5f, -0.5f,
													0.5f, 0.5f, -0.5f,
													0.5f, -0.5f, -0.5f,
													-0.5f, -0.5f, -0.5f,

													0.5f, -0.5f, 0.5f,
													0.5f, -0.5f, -0.5f,
													0.5f, 0.5f, -0.5f,
													0.5f, 0.5f, 0.5f,

													-0.5f, -0.5f, -0.5f,
													-0.5f, -0.5f, 0.5f,
													-0.5f, 0.5f, 0.5f,
													-0.5f, 0.5f, -0.5f
													};

	private static final byte[]		indices			= {
													3, 2, 1, 0,
													7, 6, 5, 4,
													4, 5, 2, 3,
													0, 1, 6, 7,
													11, 10, 9, 8,
													15, 14, 13, 12
													};

	private static final float[]	texCoords		= {
													0, 0, 1, 0, 1, 1, 0, 1,
													0, 0, 1, 0, 1, 1, 0, 1,
													0, 0, 1, 0, 1, 1, 0, 1,
													0, 0, 1, 0, 1, 1, 0, 1
													};

	public CubeQuads(Texture texture) {
		super(texture, vertices, indices, texCoords, primitiveMode);
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
	}

	@Override
	protected void drawVao() {
		vertexArrayObject.draw();
	}


	@Override
	public void update() {

	}



}
