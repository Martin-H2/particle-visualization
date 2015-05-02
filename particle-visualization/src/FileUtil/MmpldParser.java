package FileUtil;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;


public class MmpldParser {

	public static void parse(String fileName) throws DataFormatException, IOException {
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

		System.out.println("Data set ClippingBox:"
				+ "\n	MIN:	" + readFloatLE(fileStream) + " / " + readFloatLE(fileStream) + " / " + readFloatLE(fileStream)
				+ "\n	MAX:	" + readFloatLE(fileStream) + " / " + readFloatLE(fileStream) + " / " + readFloatLE(fileStream));


		//TODO: do this for all frames and save to structure...
		System.out.println("================== 1st FRAME parse test ====================");
		long frameByteOffset = readLongLE(fileStream);
		System.out.println("frameByteOffset: " + frameByteOffset);
		fileStream.seek(frameByteOffset);

		System.out.println("frame number: " + readIntLE(fileStream));

		VertexDataType vertexDataType = VertexDataType.enumCache[fileStream.readByte()];
		System.out.println("vertex data type: " + vertexDataType);

		ColorDataType colorDataType = ColorDataType.enumCache[fileStream.readByte()];
		System.out.println("color data type: " + colorDataType);

		float globalRadius = -1;
		if (vertexDataType==VertexDataType.FLOAT_XYZ || vertexDataType==VertexDataType.SHORT_XYZ) {
			globalRadius = readFloatLE(fileStream);
			System.out.println("global radius: " + globalRadius);
		}

		if (colorDataType==ColorDataType.NONE) {
			System.out.println("global color RGBA: " + fileStream.readUnsignedByte()
					+ " / " + fileStream.readUnsignedByte()
					+ " / " + fileStream.readUnsignedByte()
					+ " / " + fileStream.readUnsignedByte());
		}
		else if (colorDataType==ColorDataType.FLOAT_I) {
			System.out.println("global color intensity: " + readFloatLE(fileStream)
					+ " / range: " + readFloatLE(fileStream));
		}

		System.out.println("particle count: " + readLongLE(fileStream));

		if (vertexDataType!=VertexDataType.NONE) {
			//FIXME: THIS IS ONLY FOR vertex data type: FLOAT_XYZ & color data type: NONE
			for (int p=0; p<10; p++) {
				System.out.println("particle" + p + ": " + readFloatLE(fileStream) + " / " + readFloatLE(fileStream) + " / " + readFloatLE(fileStream));
			}
			//TODO: check end with fileStream.getFilePointer()
		}

		fileStream.close();
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
