package particleVisualization.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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

}
