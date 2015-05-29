package particleVisualization.rendering;

public enum HudDebugKeys {

	numFrames,
	frameNumber,
	fps,
	dataFps,
	numParticles,
	numObjects,
	camPos,
	camRot;




	@Override
	public String toString() {
		switch (this) {

		case numFrames: return "frameCount";
		case frameNumber: return "frame";
		case numObjects: return "objects";
		case numParticles: return "particles";

		default: return super.toString(); // for others: returns the name of enum constant, as contained in the declaration.

		}
	}



}
