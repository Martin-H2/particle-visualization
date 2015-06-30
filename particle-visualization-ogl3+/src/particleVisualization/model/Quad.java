package particleVisualization.model;

import org.lwjgl.opengl.GL11;
import particleVisualization.rendering.Shader;
import particleVisualization.rendering.Texture;



public class Quad extends DrawableEntity {

	private static final int		primitiveMode	= GL11.GL_QUADS;

	private static final float[]	vertices		= {
													-0.5f, 0, -0.5f,
													-0.5f, 0, 0.5f,
													0.5f, 0, -0.5f,
													0.5f, 0, 0.5f
													};

	private static final byte[]		indices			= {
													2, 3, 1, 0
													};

	private static final float[]	texCoords		= {
													0, 1,
													0, 0,
													1, 1,
													1, 0
													};

	public Quad(Texture texture) {
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
