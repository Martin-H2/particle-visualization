package particleVisualization.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

public class MiscUtils {

	private static Random	random	= new Random();


	public static float degreesToRadians(final float degrees) {
		return (float) Math.toRadians(degrees);
	}

	public static float coTangent(final float angle) {
		return (float) (1f / Math.tan(angle));
	}

	public static float clip(float value, float lowerBound, float upperBound) {
		if (upperBound < lowerBound) {
			upperBound = lowerBound;
		}
		return Math.max(lowerBound, Math.min(upperBound, value));

	}

	public static int clip(int value, int lowerBound, int upperBound) {
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


	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel

	public static FloatBuffer frameLayoutToSpeedlineLayout(List<float[]> dataFrames, int startingFrame, int frameCount, int skippedFrames, int particleCap,
			FloatBuffer targetBuffer, IntBuffer startingIndicesList, IntBuffer numberOfVerticesList, boolean jumpCompensation) {
		int particlesPerFrame = dataFrames.get(startingFrame).length / 3;
		if (particleCap > 0) {
			particlesPerFrame = Math.min(particlesPerFrame, particleCap);
		}
		int floatCount = dataFrames.get(startingFrame).length * frameCount;

		if (targetBuffer == null || targetBuffer.capacity() != floatCount) {
			targetBuffer = BufferUtils.createFloatBuffer(floatCount);
		}
		else {
			targetBuffer.clear();
		}
		startingIndicesList.clear();
		numberOfVerticesList.clear();

		float[] frame, nextFrame;
		int verticesTotal = 0;
		int verticesPerLinestrip = 0;
		int extraLineStripSpace = startingIndicesList.capacity() - particlesPerFrame;

		for (int particleIndex = 0; particleIndex < particlesPerFrame; particleIndex++) {
			startingIndicesList.put(verticesTotal);
			for (int frameOffset = 0; frameOffset < frameCount; frameOffset++) {
				frame = dataFrames.get(startingFrame - frameOffset);
				float x1 = frame[3 * particleIndex];
				float y1 = frame[3 * particleIndex + 1];
				float z1 = frame[3 * particleIndex + 2];
				targetBuffer.put(x1).put(y1).put(z1);
				verticesTotal++;
				verticesPerLinestrip++;

				//new linestrip check...
				if (startingFrame - frameOffset <= 0) {
					break;
				}

				if (jumpCompensation) {
					nextFrame = dataFrames.get(startingFrame - frameOffset - 1);
					float x2 = nextFrame[3 * particleIndex];
					float y2 = nextFrame[3 * particleIndex + 1];
					float z2 = nextFrame[3 * particleIndex + 2];
					if (x2 > x1 || Math.abs(y2 - y1) > 0.3f || Math.abs(z2 - z1) > 0.3f) {
						//line wrap...
						if (extraLineStripSpace > 0) {
							numberOfVerticesList.put(verticesPerLinestrip);
							verticesPerLinestrip = 0;
							startingIndicesList.put(verticesTotal);
							extraLineStripSpace--;
						}
						else {
							break;
						}
					}
				}

			}
			numberOfVerticesList.put(verticesPerLinestrip);
			verticesPerLinestrip = 0;
		}

		startingIndicesList.flip();
		numberOfVerticesList.flip();
		targetBuffer.flip();
		return targetBuffer;
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
