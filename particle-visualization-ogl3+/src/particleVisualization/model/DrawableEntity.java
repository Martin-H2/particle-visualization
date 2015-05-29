package particleVisualization.model;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import particleVisualization.rendering.Shader;
import particleVisualization.rendering.Texture;
import particleVisualization.rendering.VertexArrayObject;
import particleVisualization.util.MiscUtils;


public abstract class DrawableEntity extends Entity {


	private final Vector3f	modelScale	= new Vector3f(1, 1, 1);
	private final Matrix4f	modelMatrix	= new Matrix4f();

	//	private float[] 		vertices;
	public VertexArrayObject		vertexArrayObject;
	public Texture			texture;

	private Vector3f 		bBoxMin;
	private Vector3f 		bBoxMax;
	private Vector3f 		bBoxMid;


	/**
	 * primitiveMode = symbolic constant GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_LINE_STRIP_ADJACENCY, GL_LINES_ADJACENCY, GL_TRIANGLE_STRIP,
	 *         GL_TRIANGLE_FAN, GL_TRIANGLES, GL_TRIANGLE_STRIP_ADJACENCY, GL_TRIANGLES_ADJACENCY or GL_PATCHES
	 */
	public DrawableEntity(Texture texture, float[] positions, byte[] indices, float[] texCoords, int primitiveMode) {
		this.texture = texture;
		//		this.vertices = positions;
		vertexArrayObject = new VertexArrayObject(positions, indices, texCoords, primitiveMode);
	}

	public DrawableEntity(Texture texture, float[] initialPositions, int verticesTargetCount, int primitiveMode) {
		this.texture = texture;
		//		this.vertices = initialPositions;
		vertexArrayObject = new VertexArrayObject(initialPositions, null, null, primitiveMode, verticesTargetCount);
	}

	public void draw(Shader shader) {
		if (texture != null) {
			texture.bind(); //TODO group by "Material" ? (=shader + texture)
		}
		shader.setModelMatrix(getUpdatedModelMatrix());
		vertexArrayObject.draw();
		if (texture != null) {
			texture.unbind();
		}
	}


	public Vector3f getBoundingBoxMin() {
		if (bBoxMin==null) {
			calcBoundingBox();
		}
		return bBoxMin;
	}
	public Vector3f getBoundingBoxMax() {
		if (bBoxMax==null) {
			calcBoundingBox();
		}
		return bBoxMax;
	}
	public Vector3f getBoundingBoxMid() {
		if (bBoxMid==null) {
			bBoxMid = Vector3f.sub(getBoundingBoxMax(), getBoundingBoxMin(), null);
			bBoxMid.scale(0.5f);
			Vector3f.add(bBoxMid, getBoundingBoxMin(), bBoxMid);
		}
		return bBoxMid;
	}
	public void setBoundingBoxMin(Vector3f bBoxMin) {
		this.bBoxMin = bBoxMin;
	}

	public void setBoundingBoxMax(Vector3f bBoxMax) {
		this.bBoxMax = bBoxMax;
	}

	private void calcBoundingBox() {
		// TODO calc BoundingBox from vertices if not set
	}


	public void addScale(float x, float y, float z) {
		modelScale.x += x;
		modelScale.y += y;
		modelScale.z += z;
		needsMatrixUpdate = true;
	}

	public void addScale(float s) {
		addScale(s, s, s);
	}

	public void setScale(float s) {
		modelScale.x = s;
		modelScale.y = s;
		modelScale.z = s;
		needsMatrixUpdate = true;
	}

	private void updateModelMatrix() {
		modelMatrix.setIdentity();
		Matrix4f.translate(getPosition(), modelMatrix, modelMatrix);
		Matrix4f.scale(modelScale, modelMatrix, modelMatrix);
		Matrix4f.rotate(MiscUtils.degreesToRadians(getRoll()), UNIT_VECTOR_Z, modelMatrix, modelMatrix);
		Matrix4f.rotate(MiscUtils.degreesToRadians(getYaw()), UNIT_VECTOR_Y, modelMatrix, modelMatrix);
		Matrix4f.rotate(MiscUtils.degreesToRadians(getPitch()), UNIT_VECTOR_X, modelMatrix, modelMatrix);
		Matrix4f.translate(getBoundingBoxMid().negate(null), modelMatrix, modelMatrix);
	}

	public Matrix4f getUpdatedModelMatrix() {
		if (needsMatrixUpdate) {
			updateModelMatrix();
			needsMatrixUpdate = false;
		}
		return modelMatrix;
	}

	public void destroy() {
		if (texture != null) {
			texture.destroy();
		}
		vertexArrayObject.destroy();
	}

}
