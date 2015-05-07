package particleVisualization.model;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import particleVisualization.util.ColorDataType;
import particleVisualization.util.Stopwatch;
import particleVisualization.util.VertexDataType;


public class MmpldData {

	private final Vector3f boxMin;
	private final Vector3f boxMax;
	private final List<Vector3f[]> dataFrames;
	private final Vector4f globalRgba;


	public MmpldData(Vector3f boxMin, Vector3f boxMax, List<Vector3f[]> dataFrames, Vector4f globalRgba) {
		this.boxMin = boxMin;
		this.boxMax = boxMax;
		this.dataFrames = dataFrames;
		this.globalRgba = globalRgba;
	}



	public Vector3f getBoxMin() {
		return boxMin;
	}
	public Vector3f getBoxMax() {
		return boxMax;
	}
	public List<Vector3f[]> getDataFrames() {
		return dataFrames;
	}
	public Vector4f getGlobalRgba() {
		return globalRgba;
	}










	public static MmpldData parseFrom(String fileName) throws DataFormatException, IOException {
		RandomAccessFile fileStream = new RandomAccessFile(fileName, "r");

		String header = readAsciiString(fileStream, 6);
		if(header.startsWith("MMPLD")) {
			System.out.println("found MMPLD header...");
		} else throw new DataFormatException("MMPLD header not found.");

		short versionNumber = readShortLE(fileStream);
		if(versionNumber == 100) {
			System.out.println("found correct version number (" + versionNumber + ") ...");
		} else throw new DataFormatException("MMPLD file has wrong version.");

		int numberOfDataFrames = readIntLE(fileStream);
		if (numberOfDataFrames >= 1) {
			System.out.println("number of dataFrames: " + numberOfDataFrames);
		} else throw new DataFormatException("MMPLD file has not enough dataFames");


		System.out.println("Data set BoundingBox:"
				+ "\n	MIN:	" + readFloatLE(fileStream) + " / " + readFloatLE(fileStream) + " / " + readFloatLE(fileStream)
				+ "\n	MAX:	" + readFloatLE(fileStream) + " / " + readFloatLE(fileStream) + " / " + readFloatLE(fileStream));

		Vector3f boxMin = new Vector3f(readFloatLE(fileStream), readFloatLE(fileStream), readFloatLE(fileStream));
		Vector3f boxMax = new Vector3f(readFloatLE(fileStream), readFloatLE(fileStream), readFloatLE(fileStream));
		System.out.println("Data set ClippingBox:"
				+ "\n	MIN:	" + boxMin.toString()
				+ "\n	MAX:	" + boxMax.toString());

		long[] seekTable = new long[numberOfDataFrames];
		for (int i=0; i<numberOfDataFrames; i++) {
			seekTable[i] = readLongLE(fileStream);
		}

		Vector4f globalRgba = null;

		List<Vector3f[]> dataFrames = new ArrayList<Vector3f[]>(numberOfDataFrames);
		int numFramesProcessed = 0;
		Stopwatch stopwatch = new Stopwatch();
		System.out.println("reading data-frames...");
		for (long frameByteOffset: seekTable) {
			//			System.out.println("================== FRAME parse ====================");

			//			System.out.println("frameByteOffset: " + frameByteOffset);
			fileStream.seek(frameByteOffset);

			fileStream.skipBytes(4);
			//int frameNumber = readIntLE(fileStream);
			//			System.out.println("frame number: " + frameNumber);

			VertexDataType vertexDataType = VertexDataType.enumCache[fileStream.readByte()];
			//			System.out.println("vertex data type: " + vertexDataType);

			ColorDataType colorDataType = ColorDataType.enumCache[fileStream.readByte()];
			//			System.out.println("color data type: " + colorDataType);

			float globalRadius = -1;
			if (vertexDataType==VertexDataType.FLOAT_XYZ || vertexDataType==VertexDataType.SHORT_XYZ) {
				globalRadius = readFloatLE(fileStream);
				//				System.out.println("global radius: " + globalRadius);
			}

			if (colorDataType==ColorDataType.NONE) {
				globalRgba = new Vector4f(fileStream.readUnsignedByte()/255.0f, fileStream.readUnsignedByte()/255.0f, fileStream.readUnsignedByte()/255.0f, fileStream.readUnsignedByte()/255.0f);
				//				System.out.println("global color RGBA: " + globalRgba.toString());
			}
			else if (colorDataType==ColorDataType.FLOAT_I) {
				System.out.println("global color intensity: " + readFloatLE(fileStream)
						+ " / range: " + readFloatLE(fileStream));
			}

			int pCount = (int) readLongLE(fileStream);
			//			System.out.println("particle count: " + pCount);

			if (vertexDataType!=VertexDataType.NONE) {
				//FIXME: THIS IS ONLY FOR vertex data type: FLOAT_XYZ & color data type: NONE
				Vector3f[] particles = new Vector3f[pCount];
				for (int p=0; p<pCount; p++) {
					particles[p] = new Vector3f(readFloatLE(fileStream), readFloatLE(fileStream), readFloatLE(fileStream));
					//System.out.println("particle" + p + ": " +  particles[p].toString());
				}
				//				System.out.println("particle #0: " + particles[0]);
				dataFrames.add(particles);
				//TODO: particle bounding box checks etc ?
				//TODO: check end with fileStream.getFilePointer()
			}
			numFramesProcessed++;
			if (numFramesProcessed%100 == 0) {
				System.out.println("numFramesProcessed: " + numFramesProcessed + " (" + stopwatch.getElapsedSeconds() + "s)");
			}
			if (numFramesProcessed == 100) {
				break;
			}
		}

		fileStream.close();
		return new MmpldData(boxMin, boxMax, dataFrames, globalRgba);
	}


	private static String readAsciiString(DataInput dataInput, int numberOfBytes) throws IOException {
		byte[] bytes = new byte[numberOfBytes];
		dataInput.readFully(bytes);
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	private static short readShortLE(DataInput dataInput) throws IOException {
		byte[] bytes = new byte[2];
		dataInput.readFully(bytes);
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}

	private static int readIntLE(DataInput dataInput) throws IOException {
		byte[] bytes = new byte[4];
		dataInput.readFully(bytes);
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	private static long readLongLE(DataInput dataInput) throws IOException {
		byte[] bytes = new byte[8];
		dataInput.readFully(bytes);
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

	private static float readFloatLE(DataInput dataInput) throws IOException {
		byte[] bytes = new byte[4];
		dataInput.readFully(bytes);
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
	}


}
