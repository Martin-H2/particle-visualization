package particleVisualization.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

public class MiscUtils {



	public static float degreesToRadians(final float degrees) {
		return (float) Math.toRadians(degrees);
	}

	public static float coTangent(final float angle) {
		return (float) (1f / Math.tan(angle));
	}

	public static float clamp(float value, float lowerBound, float upperBound) {
		if (upperBound < lowerBound) {
			upperBound = lowerBound;
		}
		return Math.max(lowerBound, Math.min(upperBound, value));

	}

	public static int clamp(int value, int lowerBound, int upperBound) {
		if (upperBound < lowerBound) {
			upperBound = lowerBound;
		}
		return Math.max(lowerBound, Math.min(upperBound, value));

	}


	public static ByteBuffer createByteBuffer(byte[] array) {
		ByteBuffer bb = BufferUtils.createByteBuffer(array.length);
		bb.put(array).flip();
		return bb;
	}

	public static FloatBuffer createFloatBuffer(float[] array) {
		FloatBuffer fb = BufferUtils.createFloatBuffer(array.length);
		fb.put(array).flip();
		return fb;
	}

	public static IntBuffer createIntBuffer(int[] array) {
		IntBuffer ib = BufferUtils.createIntBuffer(array.length);
		ib.put(array).flip();
		return ib;
	}

	public static String formatVec3(Vector3f v) {
		return "(" + Math.round(v.x) + "," + Math.round(v.y) + "," + Math.round(v.z) + ")";
	}

	public static float[] cornerVectorsToQuadstrip(Vector3f boxMin, Vector3f boxMax) {
		return new float[] {
			boxMin.x, boxMax.y, boxMin.z,
			boxMin.x, boxMin.y, boxMin.z,
			boxMax.x, boxMax.y, boxMin.z,
			boxMax.x, boxMin.y, boxMin.z,
			boxMax.x, boxMax.y, boxMax.z,
			boxMax.x, boxMin.y, boxMax.z,
			boxMin.x, boxMax.y, boxMax.z,
			boxMin.x, boxMin.y, boxMax.z,
			boxMin.x, boxMax.y, boxMin.z,
			boxMin.x, boxMin.y, boxMin.z,
		};
	}



	/**
	 * 3rd degree centered binomial filter
	 *
	 * @param prevValue
	 *            - previous value (t-1)
	 * @param value
	 *            - current value to be filtered
	 * @param nextValue
	 *            - next value (t+1)
	 * @param filterKernel
	 *            - 0 ... no filtering, 1 ... max filtering
	 * @return the filtred value
	 */
	static float binomialFilter(float prevValue, float value, float nextValue, float filterKernel) {
		return filterKernel / 2f * prevValue + (1 - filterKernel) * value + filterKernel / 2f * nextValue;
	}

	public static String vertexLayoutToString(float[] vertices, int floatsPerVertex, int vertexCap) {
		StringBuilder output = new StringBuilder();
		output.append("[ ");
		for (int i = 0; i < vertices.length; i++) {
			if (i != 0) {
				if (i % floatsPerVertex == 0) {
					output.append(" | ");
				}
				else {
					output.append(", ");
				}
			}
			output.append(vertices[i]);
			if ((i + 1) / floatsPerVertex >= vertexCap) {
				break;
			}
		}
		output.append(" ]");
		return output.toString();
	}

	public static String vertexLayoutToString(FloatBuffer fb, int floatsPerVertex, int vertexCap) {
		float[] a = new float[fb.limit()];
		fb.get(a);
		return vertexLayoutToString(a, floatsPerVertex, vertexCap);
	}


}
