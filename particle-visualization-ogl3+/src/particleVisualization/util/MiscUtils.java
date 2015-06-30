package particleVisualization.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

public class MiscUtils {



	public static float degreesToRadians(final float degrees) { //TODO kill
		return (float) Math.toRadians(degrees);
	}

	public static float coTangent(final float angle) {
		return (float) (1f / Math.tan(angle));
	}

	public static float clip(float value, float lowerBound, float upperBound) {
		return Math.max(lowerBound, Math.min(upperBound, value));

	}

	public static int clip(int value, int lowerBound, int upperBound) {
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


	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel

	public static FloatBuffer frameLayoutToSpeedlineLayout(List<float[]> dataFrames, int startingFrame, int frameCount, FloatBuffer fb, int particleCap) {
		int particleCount = dataFrames.get(startingFrame).length / 3;
		if (particleCap > 0) {
			particleCount = Math.min(particleCount, particleCap);
		}
		int floatCount = dataFrames.get(startingFrame).length * frameCount * 2 - 2 * particleCount;

		if (fb == null || fb.capacity() != floatCount) {
			fb = BufferUtils.createFloatBuffer(floatCount);
		}
		else {
			fb.clear();
		}


		for (int p = 0; p < particleCount; p++) {
			for (int f = 0; f < frameCount - 1; f++) {
				putVertex(fb, p, dataFrames.get(startingFrame + f), dataFrames.get(startingFrame + f + 1));
			}
			//fb.put(1000); //TODO restart index ?
		}

		fb.flip();
		return fb;
	}

	private static void putVertex(FloatBuffer fb, int particleIndex, float[] frame, float[] nextFrame) {
		float x1 = frame[3 * particleIndex];
		float y1 = frame[3 * particleIndex + 1];
		float z1 = frame[3 * particleIndex + 2];
		float x2 = nextFrame[3 * particleIndex];
		float y2 = nextFrame[3 * particleIndex + 1];
		float z2 = nextFrame[3 * particleIndex + 2];
		if (x2 > x1 && Math.abs(y2 - y1) < 0.3f && Math.abs(z2 - z1) < 0.3f) {
			fb.put(x1).put(y1).put(z1);
			fb.put(x2).put(y2).put(z2);
		}
	}

	public static String vertexLayoutToString(float[] vertices, int floatsPerVertex, int vertexCount) {
		StringBuilder output = new StringBuilder();
		output.append("[ ");
		for (int i = 0; i < floatsPerVertex * vertexCount; i++) {
			if (i != 0)
				if (i % floatsPerVertex == 0) {
					output.append(" | ");
				}
				else {
					output.append(", ");
				}
			output.append(vertices[i]);
		}
		output.append(" ]");
		return output.toString();
	}

	public static String vertexLayoutToString(FloatBuffer fb, int floatsPerVertex, int vertexCount) {
		float[] a = new float[floatsPerVertex * vertexCount];
		fb.get(a);
		return vertexLayoutToString(a, floatsPerVertex, vertexCount);
	}
}
