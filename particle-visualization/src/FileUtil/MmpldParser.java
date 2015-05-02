package FileUtil;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;


public class MmpldParser {

	public static void parse(FileInputStream fileInputStream) throws DataFormatException, IOException {
		DataInputStream dataInput = new DataInputStream(fileInputStream);

		String header = readAsciiString(dataInput, 6);
		if(header.startsWith("MMPLD")) {
			System.out.println("found MMPLD header...");
		} else throw new DataFormatException("MMPLD header not found.");

		short versionNumber = readShortLE(dataInput);
		if(versionNumber == 100) {
			System.out.println("found correct version number (" + versionNumber + ") ...");
		} else throw new DataFormatException("MMPLD file has wrong version.");

		int numberOfDataFrames = readIntLE(dataInput);
		if (numberOfDataFrames >= 1) {
			System.out.println("number of dataFrames: " + numberOfDataFrames);
		} else throw new DataFormatException("MMPLD file has not enough dataFames");


		dataInput.close();
	}


	private static String readAsciiString(DataInput dataInput, int numberOfBytes) throws IOException {
		byte[] bytes = new byte[numberOfBytes];
		dataInput.readFully(bytes);
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	private static short readShortLE(DataInputStream dataInput) throws IOException {
		byte[] bytes = new byte[2];
		dataInput.readFully(bytes);
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}

	private static int readIntLE(DataInputStream dataInput) throws IOException {
		byte[] bytes = new byte[4];
		dataInput.readFully(bytes);
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

}
