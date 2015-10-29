package particleVisualization.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.Map.Entry;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;


public class VertexSorter {

	static int[]				sortingIndices;
	static Map<Integer, Float>	drawOrderIndexMapping;


	public static void generateSortingIndices(float[] keyFrame, int numberOfVertices, Matrix4f modelViewMatrix) {
		sortingIndices = getVertexSortingIndices(keyFrame, false, 2, numberOfVertices, modelViewMatrix);
	}


	public static FloatBuffer fillParticleBuffer(List<float[]> dataFrames, int currentFrameIndex, int maxParticlesDisplayed, FloatBuffer targetBuffer) {
		int floatCount = maxParticlesDisplayed * 3;
		if (targetBuffer == null || targetBuffer.capacity() < floatCount) {
			targetBuffer = BufferUtils.createFloatBuffer(floatCount);
			System.out.println("#fillParticleBuffer.createFloatBuffer !");
		}
		else {
			targetBuffer.clear();
		}

		for (int i = 0; i < maxParticlesDisplayed; i++) {
			targetBuffer
					.put(getX(dataFrames, currentFrameIndex, sortingIndices[i]))
					.put(getY(dataFrames, currentFrameIndex, sortingIndices[i]))
					.put(getZ(dataFrames, currentFrameIndex, sortingIndices[i]));
		}

		targetBuffer.flip();
		return targetBuffer;
	}

	public static FloatBuffer fillParticleColorBuffer(List<float[]> dataFramesColors, int currentFrameIndex, int maxParticlesDisplayed, FloatBuffer targetBuffer) {
		if (targetBuffer == null || targetBuffer.capacity() < maxParticlesDisplayed) {
			targetBuffer = BufferUtils.createFloatBuffer(maxParticlesDisplayed);
			System.out.println("#fillParticleColorBuffer.createFloatBuffer !");
		}
		else {
			targetBuffer.clear();
		}

		for (int i = 0; i < maxParticlesDisplayed; i++) {
			targetBuffer.put(dataFramesColors.get(currentFrameIndex)[sortingIndices[i]]);
		}

		targetBuffer.flip();
		return targetBuffer;
	}


	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel
	// [xyz xyz xyz ...] partikel
	// z desc
	public static FloatBuffer frameLayoutToSpeedlineLayout(List<float[]> dataFrames, int startingFrame, int frameCount, int particleStartingIndex, int particleCap,
			FloatBuffer targetBuffer, FloatBuffer lineStripOffsets, IntBuffer startingIndicesList, IntBuffer numberOfVerticesList,
			boolean jumpCompensation, float filterKernel, Vector3f jumpThresholds) {


		int maxParticlesDisplayed = dataFrames.get(startingFrame).length / 3;
		if (particleCap > 0) {
			maxParticlesDisplayed = Math.min(maxParticlesDisplayed, particleCap);
		}
		int floatCount = dataFrames.get(startingFrame).length * frameCount;


		if (targetBuffer == null || targetBuffer.capacity() < floatCount) {
			targetBuffer = BufferUtils.createFloatBuffer(floatCount * 2 + dataFrames.get(startingFrame).length * 10);
			System.out.println("#frameLayoutToSpeedlineLayout.createFloatBuffer !");
		}
		else {
			targetBuffer.clear();
		}
		startingIndicesList.clear();
		numberOfVerticesList.clear();
		lineStripOffsets.clear();

		int verticesTotal = 0;
		int verticesPerLinestrip = 0;
		//int extraLineStripSpace = startingIndicesList.capacity() - particlesPerFrame;

		for (int i = particleStartingIndex; i < particleStartingIndex + maxParticlesDisplayed; i++) {
			int particleIndex = sortingIndices[i];
			startingIndicesList.put(verticesTotal);
			for (int frameOffset = 0; frameOffset < frameCount; frameOffset++) {
				// guard
				if (startingFrame - frameOffset < 0 || startingFrame - frameOffset > dataFrames.size()) {
					break;
				}


				// init
				boolean isParticleJump = false;
				boolean isLinestripEnd = frameOffset == 0 || frameOffset == frameCount - 1
					|| startingFrame - frameOffset <= 0 || startingFrame - frameOffset >= dataFrames.size() - 1;

				float x1 = getX(dataFrames, startingFrame - frameOffset, particleIndex);
				float y1 = getY(dataFrames, startingFrame - frameOffset, particleIndex);
				float z1 = getZ(dataFrames, startingFrame - frameOffset, particleIndex);
				float x2 = x1, y2 = y1, z2 = z1;

				if (startingFrame - frameOffset - 1 >= 0) {
					x2 = getX(dataFrames, startingFrame - frameOffset - 1, particleIndex);
					y2 = getY(dataFrames, startingFrame - frameOffset - 1, particleIndex);
					z2 = getZ(dataFrames, startingFrame - frameOffset - 1, particleIndex);
				}
				isParticleJump = Math.abs(x2 - x1) >= jumpThresholds.x || Math.abs(y2 - y1) >= jumpThresholds.y || Math.abs(z2 - z1) >= jumpThresholds.z;


				// do
				if (!isLinestripEnd && !isParticleJump) {
					targetBuffer.put(MiscUtils.binomialFilter(
							x2,
							x1,
							getX(dataFrames, startingFrame - frameOffset + 1, particleIndex),
							filterKernel));
					targetBuffer.put(MiscUtils.binomialFilter(
							y2,
							y1,
							getY(dataFrames, startingFrame - frameOffset + 1, particleIndex),
							filterKernel));
					targetBuffer.put(MiscUtils.binomialFilter(
							z2,
							z1,
							getZ(dataFrames, startingFrame - frameOffset + 1, particleIndex),
							filterKernel));
				}
				else {
					targetBuffer.put(x1).put(y1).put(z1);
				}



				lineStripOffsets.put(frameOffset / (frameCount - 1f));
				verticesTotal++;
				verticesPerLinestrip++;


				if (jumpCompensation && isParticleJump) {
					//line wrap...
					//					if (extraLineStripSpace > 0) {
					//						numberOfVerticesList.put(verticesPerLinestrip);
					//						verticesPerLinestrip = 0;
					//						startingIndicesList.put(verticesTotal);
					//						extraLineStripSpace--;
					//					}
					//					else {
					//						break;
					//					}
					break;
				}

			}
			numberOfVerticesList.put(verticesPerLinestrip);
			verticesPerLinestrip = 0;
		}

		startingIndicesList.flip();
		numberOfVerticesList.flip();
		lineStripOffsets.flip();
		targetBuffer.flip();
		return targetBuffer;
	}

	static float getX(List<float[]> dataFrames, int frameIndex, int particleIndex) {
		return dataFrames.get(frameIndex)[3 * particleIndex];
	}

	static float getY(List<float[]> dataFrames, int frameIndex, int particleIndex) {
		return dataFrames.get(frameIndex)[3 * particleIndex + 1];
	}

	static float getZ(List<float[]> dataFrames, int frameIndex, int particleIndex) {
		return dataFrames.get(frameIndex)[3 * particleIndex + 2];
	}

	public static int[] getVertexSortingIndices(float[] vertexArray3f, boolean ascending, int floatOffset, int numberOfVertices, Matrix4f modelViewMatrix) {
		if (drawOrderIndexMapping == null || drawOrderIndexMapping.size() != numberOfVertices) {
			drawOrderIndexMapping = new HashMap<Integer, Float>(numberOfVertices);
		}

		Vector4f temp = new Vector4f();

		//		System.out.println("numberOfVertices: " + numberOfVertices + ", vertexArray3f.length: " + vertexArray3f.length);

		for (int i = 0; i < numberOfVertices; i++) {
			Matrix4f.transform(modelViewMatrix, new Vector4f(vertexArray3f[3 * i], vertexArray3f[3 * i + 1], vertexArray3f[3 * i + 2], 1), temp);
			drawOrderIndexMapping.put(i, -temp.z);


			//			drawOrderIndexMapping.put(i,
			//					modelViewMatrix.m20 * vertexArray3f[3 * i]
			//						+ modelViewMatrix.m21 * vertexArray3f[3 * i + 1]
			//						+ modelViewMatrix.m22 * vertexArray3f[3 * i + 2]
			//					);

		}


		List<Entry<Integer, Float>> entryList = new ArrayList<Entry<Integer, Float>>(drawOrderIndexMapping.entrySet());

		Collections.sort(entryList, new Comparator<Entry<?, Float>>() {

			@Override
			public int compare(Entry<?, Float> e1, Entry<?, Float> e2) {
				return e1.getValue().compareTo(e2.getValue()) * (ascending ? 1 : -1);
			}
		});

		int[] result = new int[numberOfVertices];
		for (int i = 0; i < numberOfVertices; i++) {
			result[i] = entryList.get(i).getKey();
		}

		return result;
	}

}
