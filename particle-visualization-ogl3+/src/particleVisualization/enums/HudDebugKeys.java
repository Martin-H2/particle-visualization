package particleVisualization.enums;

public enum HudDebugKeys {

	fps,
	frametime,
	gpuMemMb,
	vSync,
	dataFrameCount,
	dataFrame,
	dataFps,
	particleCount,
	numTailSegments,
	particles,
	numObjects,
	camPos,
	camRot,
	filterKernel,
	speedlineTransparency,
	textureFact,
	textureYScale;



	@Override
	public String toString() {
		switch (this) {

		case speedlineTransparency:
			return "transparency";
		case dataFrameCount:
			return "dataFrames";
		case numObjects:
			return "tris";
		case numTailSegments:
			return "tailSegments";
		case particleCount:
			return "particles";

		default:
			return super.toString(); // for others: returns the name of enum constant, as contained in the declaration.

		}
	}



}
