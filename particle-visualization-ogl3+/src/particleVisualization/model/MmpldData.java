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
import particleVisualization.enums.ColorDataType;
import particleVisualization.enums.VertexDataType;

public class MmpldData {

	protected static int			maxFramesRead;
	protected static final boolean	READ_PARALLEL		= true;
	private static final int		MAX_BUFFERSIZE_MB	= 1000;
	//private static final Logger log = Logger.getLogger( MmpldData.class.getName() );


	private final Vector3f			boxMin;
	private final Vector3f			boxMax;
	private final List<float[]>		dataFrames;
	private final int				maxParticlesPerFrame;
	private final int				numberOfDataFrames;
	private final float				globalRadius;
	private final List<float[]>		dataFramesColors;
	private final boolean			hasColorData;
	private final String			fileName;


	public MmpldData(final Vector3f boxMin, final Vector3f boxMax, final List<float[]> dataFrames, List<float[]> dataFramesColors, float globalRadius, final int particlesPerFrame, int numberOfDataFrames, String fileName) {
		this.boxMin = boxMin;
		this.boxMax = boxMax;
		this.dataFrames = dataFrames;
		this.dataFramesColors = dataFramesColors;
		this.globalRadius = globalRadius;
		maxParticlesPerFrame = particlesPerFrame;
		this.numberOfDataFrames = numberOfDataFrames;
		hasColorData = dataFramesColors != null && !dataFramesColors.isEmpty();
		this.fileName = fileName;
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
		//			+ "\n	MIN:	" + byteBuffer.getFloat() + " / " + byteBuffer.getFloat() + " / " + byteBuffer.getFloat()
		//			+ "\n	MAX:	" + byteBuffer.getFloat() + " / " + byteBuffer.getFloat() + " / " + byteBuffer.getFloat());

		Vector3f boxMin = new Vector3f(byteBuffer.getFloat(), byteBuffer.getFloat(), byteBuffer.getFloat());
		Vector3f boxMax = new Vector3f(byteBuffer.getFloat(), byteBuffer.getFloat(), byteBuffer.getFloat());
		System.out.println("Data set ClippingBox:"
			+ "\n	MIN:	" + boxMin.getX() + " / " + boxMin.getY() + " / " + boxMin.getZ()
			+ "\n	MAX:	" + boxMax.getX() + " / " + boxMax.getY() + " / " + boxMax.getZ());

		// SEEK TABLE
		byteBuffer = ByteBuffer.allocateDirect((numberOfDataFrames + 1) * 8).order(ByteOrder.LITTLE_ENDIAN);
		fileInputChannel.read(byteBuffer);
		byteBuffer.flip();
		System.out.println("reading " + byteBuffer.capacity() / 1024 + " kb long seek-table...");
		long[] seekTable = new long[numberOfDataFrames + 1];
		for (int i = 0; i < numberOfDataFrames + 1; i++) {
			seekTable[i] = byteBuffer.getLong();
		}


		//Stopwatch stopwatch = new Stopwatch();



		// 1st DATA FRAME
		System.out.println("reading 1st data-frame...");
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



		maxFramesRead = (int) (MAX_BUFFERSIZE_MB / (fileInputChannel.size() / 1024d / 1024d / numberOfDataFrames));
		System.out.println("limiting buffer to " + MAX_BUFFERSIZE_MB + "mb (" + maxFramesRead + " frames)");
		final List<float[]> dataFrames = new ArrayList<float[]>(Math.min(numberOfDataFrames, maxFramesRead));
		final List<float[]> dataFramesColors = new ArrayList<float[]>(Math.min(numberOfDataFrames, maxFramesRead));


		FrameMetaData frameMetaData = addParticleFrames(byteBuffer, dataFrames, dataFramesColors, true);


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


					try {
						addParticleFrames(byteBuffer, dataFrames, dataFramesColors, false);
					}
					catch (DataFormatException e1) {
						e1.printStackTrace();
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
					if (i + 1 == maxFramesRead) {
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
			System.out.println("warning - parallel mode off, loading " + maxFramesRead + " frames at once...");
			frameLoader.run();
		}

		mmpldData = new MmpldData(boxMin, boxMax, dataFrames, dataFramesColors, frameMetaData.getRadius(), frameMetaData.getParticlesPerFrame(),
				Math.min(numberOfDataFrames, maxFramesRead), mmpldFile.getName());

		return mmpldData;
	}


	private static FrameMetaData addParticleFrames(ByteBuffer byteBuffer, List<float[]> framePosList, List<float[]> frameColorList, boolean isFirstFrame) throws DataFormatException {
		int particlesPerFrame = 0;
		float globalRadius = -1;
		float particleListRgbaPacked = 0;
		VertexDataType vertexDataType;
		ColorDataType colorDataType;

		int numberOfParticleLists = byteBuffer.getInt();
		if (isFirstFrame) {
			System.out.println("numberOfParticleLists: " + numberOfParticleLists);
		}

		float[] positions = null;
		float[] colors = null;

		// ==== read particle lists =====
		for (int listIndex = 0; listIndex < numberOfParticleLists; listIndex++) {

			vertexDataType = VertexDataType.enumCache[byteBuffer.get()];
			colorDataType = ColorDataType.enumCache[byteBuffer.get()];

			if (vertexDataType == VertexDataType.FLOAT_XYZ || vertexDataType == VertexDataType.SHORT_XYZ) {
				//skipBytes(byteBuffer, 4);
				globalRadius = byteBuffer.getFloat();
			}

			if (colorDataType == ColorDataType.NONE) {
				particleListRgbaPacked = packRGBA(getUnsignedByte(byteBuffer), getUnsignedByte(byteBuffer), getUnsignedByte(byteBuffer), getUnsignedByte(byteBuffer));
			}
			else if (colorDataType == ColorDataType.FLOAT_I) {
				//				System.out.println("global color intensity: " + byteBuffer.getFloat()
				//					+ " / range: " + byteBuffer.getFloat());
				skipBytes(byteBuffer, 8);
			}

			if (vertexDataType != VertexDataType.FLOAT_XYZ) throw new DataFormatException("only VertexDataType 'FLOAT_XYZ' implemented");
			if (colorDataType != ColorDataType.NONE && colorDataType != ColorDataType.FLOAT_RGBA)
				throw new DataFormatException("only ColorDataType 'NONE' and 'FLOAT_RGBA' implemented"); //TODO implement more formats

			int particlesPerList = (int) byteBuffer.getLong();


			if (isFirstFrame) {
				System.out.println("\n=== frame 1, particleList " + listIndex + " ===");
				System.out.println("vertex data type: " + vertexDataType);
				System.out.println("color data type: " + colorDataType);
				System.out.println("global radius: " + globalRadius);
				System.out.println("particlesPerFrame: " + particlesPerList);
			}



			if (positions == null) {
				particlesPerFrame = particlesPerList * numberOfParticleLists; //TODO approx !!
				positions = new float[particlesPerFrame * 3]; //TODO use buffer directly ?
				colors = new float[particlesPerFrame];
			}

			int baseIndex = listIndex * particlesPerList;
			for (int p = baseIndex; p < baseIndex + particlesPerList; p++) {
				//			System.out.println("\n===================================== particle #" + p);
				//			System.out.println("x: " + byteBuffer.getFloat());
				//			System.out.println("y: " + byteBuffer.getFloat());
				//			System.out.println("z: " + byteBuffer.getFloat());
				//			System.out.println("r: " + byteBuffer.getFloat());
				//			System.out.println("g: " + byteBuffer.getFloat());
				//			System.out.println("b: " + byteBuffer.getFloat());
				//			System.out.println("a: " + byteBuffer.getFloat());
				positions[3 * p] = byteBuffer.getFloat();
				positions[3 * p + 1] = byteBuffer.getFloat();
				positions[3 * p + 2] = byteBuffer.getFloat();
				if (colorDataType == ColorDataType.FLOAT_RGBA) {
					colors[p] = packRGBA(
							floatToIntColor(byteBuffer.getFloat()),
							floatToIntColor(byteBuffer.getFloat()),
							floatToIntColor(byteBuffer.getFloat()),
							floatToIntColor(byteBuffer.getFloat())
						);
					//skipBytes(sourceBuffer, 16);
				}
				else {
					colors[p] = particleListRgbaPacked;
				}
			}
			//			if (colorDataType == ColorDataType.FLOAT_RGBA) {
			frameColorList.add(colors);
			//			}
			framePosList.add(positions);
		}
		return new FrameMetaData(globalRadius, particlesPerFrame);
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



	public float getGlobalRadius() {
		return globalRadius;
	}

	public static int floatToIntColor(float f) {
		return (int) (255 * f);
	}

	public static float packRGBA(int r, int g, int b, int a) {
		return Float.intBitsToFloat(r << 0 | g << 8 | b << 16 | a << 24);
	}



	public List<float[]> getDataFramesColors() {
		return dataFramesColors;
	}



	public boolean isColorDataSet() {
		return hasColorData;
	}



	public String getFileName() {
		return fileName;
	}



	private static class FrameMetaData {

		private final float	globalRadius;
		private final int	particlesPerFrame;

		public FrameMetaData(float globalRadius, int particlesPerFrame) {
			this.globalRadius = globalRadius;
			this.particlesPerFrame = particlesPerFrame;
		}

		public int getParticlesPerFrame() {
			return particlesPerFrame;
		}

		public float getRadius() {
			return globalRadius;
		}

	}

}
