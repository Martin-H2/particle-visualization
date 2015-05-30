package particleVisualization.enums;

public enum HudDebugKeys {

	fps,
	dataFrameCount,
	dataFrame,
	dataFps,
	particleCount,
	numTailSegments,
	numObjects,
	camPos,
	camRot;



	@Override
	public String toString() {
		switch (this) {

		case dataFrameCount:
			return "dataFrames";
		case numObjects:
			return "objects";
		case numTailSegments:
			return "tailSegments";
		case particleCount:
			return "particles";

		default:
			return super.toString(); // for others: returns the name of enum constant, as contained in the declaration.

		}
	}



}
