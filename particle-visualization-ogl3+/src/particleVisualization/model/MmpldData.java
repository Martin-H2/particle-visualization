package particleVisualization.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import particleVisualization.enums.ColorDataType;
import particleVisualization.enums.VertexDataType;

public class MmpldData {

	protected static final int		MAX_FRAMES_READ	= 3000;
	protected static final boolean	READ_PARALLEL	= true;
	//private static final Logger log = Logger.getLogger( MmpldData.class.getName() );


	private final Vector3f			boxMin;
	private final Vector3f			boxMax;
	private final List<float[]>		dataFrames;
	private final Vector4f			globalRgba;
	private final int				maxParticlesPerFrame;
	private final int				numberOfDataFrames;


	public MmpldData(final Vector3f boxMin, final Vector3f boxMax, final List<float[]> dataFrames, final Vector4f globalRgba, final int particlesPerFrame, int numberOfDataFrames) {
		this.boxMin = boxMin;
		this.boxMax = boxMax;
		this.dataFrames = dataFrames;
		this.globalRgba = globalRgba;
		maxParticlesPerFrame = particlesPerFrame;
		this.numberOfDataFrames = numberOfDataFrames;
	}



	public Vector3f getBoxMin() {
		return boxMin;
	}

	public Vector3f getBoxMax() {
		return boxMax;
	}

	public List<float[]> getDataFrames() {
		return dataFrames;
	}

	public Vector4f getGlobalRgba() {
		return globalRgba;
	}

	public int getNumberOfDataFrames() {
		return numberOfDataFrames;
	}

	public int getParticlesPerFrame() {
		return maxParticlesPerFrame;
	}



	public static MmpldData parseFrom(final File mmpldFile) throws DataFormatException, IOException, InterruptedException {
		FileInputStream fileInputStream = new FileInputStream(mmpldFile);
		FileChannel fileInputChannel = fileInputStream.getChannel();
		MmpldData mmpldData = null;
		System.out.println("\nReading input file: " + mmpldFile);

		// 60 byte HEADER
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(60).order(ByteOrder.LITTLE_ENDIAN);
		fileInputChannel.read(byteBuffer);
		byteBuffer.flip();

		String magicId = readAsciiString(byteBuffer, 6);
		if (magicId.startsWith("MMPLD")) {
			System.out.println("found MMPLD header...");
		}
		else throw new DataFormatException("MMPLD header not found.");

		short versionNumber = byteBuffer.getShort();
		if (versionNumber == 100) {
			System.out.println("found correct version number (" + versionNumber + ") ...");
		}
		else throw new DataFormatException("MMPLD file has wrong version.");

		int numberOfDataFrames = byteBuffer.getInt();
		if (numberOfDataFrames >= 1) {
			System.out.println("number of dataFrames: " + numberOfDataFrames);
		}
		else throw new DataFormatException("MMPLD file has not enough dataFames");

		skipBytes(byteBuffer, 24);
		//		System.out.println("Data set BoundingBox:"
		//				+ "\n	MIN:	" + byteBuffer.getFloat() + " / " + byteBuffer.getFloat() + " / " + byteBuffer.getFloat()
		//				+ "\n	MAX:	" + byteBuffer.getFloat() + " / " + byteBuffer.getFloat() + " / " + byteBuffer.getFloat());

		Vector3f boxMin = new Vector3f(byteBuffer.getFloat(), byteBuffer.getFloat(), byteBuffer.getFloat());
		Vector3f boxMax = new Vector3f(byteBuffer.getFloat(), byteBuffer.getFloat(), byteBuffer.getFloat());
		//		System.out.println("Data set ClippingBox:"
		//				+ "\n	MIN:	" + boxMin.getX() + " / " + boxMin.getY() + " / " + boxMin.getZ()
		//				+ "\n	MAX:	" + boxMax.getX() + " / " + boxMax.getY() + " / " + boxMax.getZ());

		// SEEK TABLE
		byteBuffer = ByteBuffer.allocateDirect((numberOfDataFrames + 1) * 8).order(ByteOrder.LITTLE_ENDIAN);
		fileInputChannel.read(byteBuffer);
		byteBuffer.flip();
		System.out.println("reading " + byteBuffer.capacity() / 1024 + " kb long seek-table...");
		long[] seekTable = new long[numberOfDataFrames + 1];
		for (int i = 0; i < numberOfDataFrames + 1; i++) {
			seekTable[i] = byteBuffer.getLong();
		}


		Vector4f globalRgba = null;
		List<float[]> dataFrames = new ArrayList<float[]>(numberOfDataFrames);
		//Stopwatch stopwatch = new Stopwatch();

		System.out.println("reading 1st data-frameNumber...");



		// 1st DATA FRAME
		//System.out.println("========================= frameNumber index " + i + " =========================");
		if (seekTable[1] - seekTable[0] != byteBuffer.capacity()) {
			byteBuffer = ByteBuffer.allocateDirect((int) (seekTable[1] - seekTable[0])).order(ByteOrder.LITTLE_ENDIAN);
			System.out.println("adjusting byteBuffer to next frameSize: " + byteBuffer.capacity());
		}
		else {
			byteBuffer.clear();
		}
		final int firstFrameSize = byteBuffer.capacity();
		fileInputChannel.read(byteBuffer, seekTable[0]);
		byteBuffer.flip();

		skipBytes(byteBuffer, 4);
		//System.out.println("frameNumber number: " + frameNumber);

		VertexDataType vertexDataType = VertexDataType.enumCache[byteBuffer.get()];
		//System.out.println("vertex data type: " + vertexDataType);

		ColorDataType colorDataType = ColorDataType.enumCache[byteBuffer.get()];
		//System.out.println("color data type: " + colorDataType);

		//float globalRadius = -1;
		if (vertexDataType == VertexDataType.FLOAT_XYZ || vertexDataType == VertexDataType.SHORT_XYZ) {
			skipBytes(byteBuffer, 4);
			//System.out.println("global radius: " + globalRadius);
		}

		if (colorDataType == ColorDataType.NONE) {
			globalRgba = new Vector4f(getUnsignedByte(byteBuffer), getUnsignedByte(byteBuffer), getUnsignedByte(byteBuffer), getUnsignedByte(byteBuffer));
			System.out.println("global color RGBA: " + globalRgba.toString());
			globalRgba.scale(1.0f / 255.0f);
			//System.out.println("global color RGBA scaled: " + globalRgba.toString());
		}
		else if (colorDataType == ColorDataType.FLOAT_I) {
			System.out.println("global color intensity: " + byteBuffer.getFloat()
				+ " / range: " + byteBuffer.getFloat());
		}

		int particlesPerFrame = (int) byteBuffer.getLong();
		//System.out.println("particle count: " + pCount);

		if (vertexDataType != VertexDataType.NONE) {
			//FIXME: THIS IS ONLY FOR vertex data type: FLOAT_XYZ & color data type: NONE
			float[] particles = new float[particlesPerFrame * 3]; //TODO use buffer directly ?
			//byteBuffer.asFloatBuffer().get(particles);
			for (int p = 0; p < particles.length; p++) {
				particles[p] = byteBuffer.getFloat();
				//System.out.println("particle" + p + ": " +  particles[p].toString());
			}
			//System.out.println("particle #0: " + particles[0]);
			dataFrames.add(particles);
		}



		Thread frameLoader = new Thread(new Runnable() {

			@Override
			public void run() {
				ByteBuffer byteBuffer = ByteBuffer.allocateDirect(firstFrameSize).order(ByteOrder.LITTLE_ENDIAN);

				// DATA FRAMES
				for (int i = 1; i < numberOfDataFrames; i++) {
					if (seekTable[i + 1] - seekTable[i] != byteBuffer.capacity()) {
						byteBuffer = ByteBuffer.allocateDirect((int) (seekTable[i + 1] - seekTable[i])).order(ByteOrder.LITTLE_ENDIAN);
						System.out.println("adjusting byteBuffer to next frameSize: " + byteBuffer.capacity());
					}
					else {
						byteBuffer.clear();
					}
					try {
						fileInputChannel.read(byteBuffer, seekTable[i]);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					byteBuffer.flip();
					skipBytes(byteBuffer, 4);
					VertexDataType vertexDataType = VertexDataType.enumCache[byteBuffer.get()];
					ColorDataType colorDataType = ColorDataType.enumCache[byteBuffer.get()];
					if (vertexDataType == VertexDataType.FLOAT_XYZ || vertexDataType == VertexDataType.SHORT_XYZ) {
						skipBytes(byteBuffer, 4);
					}
					if (colorDataType == ColorDataType.NONE) {
						skipBytes(byteBuffer, 4);
					}
					else if (colorDataType == ColorDataType.FLOAT_I) {
						System.out.println("global color intensity: " + byteBuffer.getFloat()
							+ " / range: " + byteBuffer.getFloat());
					}

					int pCount = (int) byteBuffer.getLong();
					if (pCount != particlesPerFrame) {
						System.err.println("error: frame #" + i + " has differing particle count. (" + pCount + " instead of " + particlesPerFrame + ")");
					}

					if (vertexDataType != VertexDataType.NONE) {
						//FIXME: THIS IS ONLY FOR vertex data type: FLOAT_XYZ & color data type: NONE
						float[] particles = new float[pCount * 3];
						for (int p = 0; p < particles.length; p++) {
							particles[p] = byteBuffer.getFloat();
						}
						dataFrames.add(particles);
					}
					if (READ_PARALLEL) {
						Thread.yield();
						if ((i + 1) % 30 == 0) {
							//						System.out.println("framesProcessed: " + (i+1) + " (" + stopwatch.getElapsedSeconds() + "s)");
							//						break;
							try {
								Thread.sleep(300);
							}
							catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					if (i + 1 == MAX_FRAMES_READ) {
						break;
					}
				}
				try {
					fileInputChannel.close();
					fileInputStream.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}

			}
		}, "frameLoaderThread");

		if (READ_PARALLEL) {
			System.out.println("starting dataFrame loader as daemon thread...");
			frameLoader.setDaemon(true);
			frameLoader.setPriority(Math.max(Thread.MIN_PRIORITY, frameLoader.getPriority() - 2));
			frameLoader.start();
		}
		else {
			frameLoader.run();
		}

		mmpldData = new MmpldData(boxMin, boxMax, dataFrames, globalRgba, particlesPerFrame, Math.min(numberOfDataFrames, MAX_FRAMES_READ));

		return mmpldData;
	}


	private static void skipBytes(final ByteBuffer byteBuffer, final int n) {
		byteBuffer.position(byteBuffer.position() + n);
	}



	private static String readAsciiString(final ByteBuffer byteBuffer, final int numberOfBytes) throws IOException {
		byte[] bytes = new byte[numberOfBytes];
		byteBuffer.get(bytes);
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	public static short getUnsignedByte(final ByteBuffer bb) {
		return (short) (bb.get() & 0xff);
	}


}
