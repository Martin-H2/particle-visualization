package particleVisualization.model;

import org.lwjgl.util.vector.Vector3f;
import particleVisualization.util.MiscUtils;


public abstract class Entity {



	protected static final Vector3f	UNIT_VECTOR_Z		= new Vector3f(0, 0, 1);
	protected static final Vector3f	UNIT_VECTOR_Y		= new Vector3f(0, 1, 0);
	protected static final Vector3f	UNIT_VECTOR_X		= new Vector3f(1, 0, 0);

	private final Vector3f			position			= new Vector3f(0, 0, 0);
	private final Vector3f			rotation			= new Vector3f(0, 0, 0);

	protected boolean				needsMatrixUpdate	= true;

	public void translate(float xDelta, float yDelta, float zDelta) {
		position.translate(xDelta, yDelta, zDelta);
		needsMatrixUpdate = true;
	}

	public void setPosition(float x, float y, float z) {
		position.set(x, y, z);
		needsMatrixUpdate = true;
	}

	public void translate(Vector3f delta) {
		translate(delta.x, delta.y, delta.z);
	}

	public void translate(Vector3f delta, float deltaScale) {
		translate(delta.x * deltaScale, delta.y * deltaScale, delta.z * deltaScale);
	}

	public void rotate(float x, float y, float z) {
		rotation.x += x;
		rotation.y += y;
		rotation.z += z;
		needsMatrixUpdate = true;
	}

	public void setPitch(float pitch) {
		rotation.x = pitch;
		needsMatrixUpdate = true;
	}

	public void setYaw(float yaw) {
		rotation.y = yaw;
		needsMatrixUpdate = true;
	}

	public void setRoll(float roll) {
		rotation.z = roll;
		needsMatrixUpdate = true;
	}

	public void addPitchClipped(float pitchDelta, float maxPitch) {
		setPitch(MiscUtils.clamp(getPitch() + pitchDelta, -maxPitch, maxPitch));
	}

	public void addPitch(float pitchDelta) {
		setPitch(getPitch() + pitchDelta);
	}

	public void addYaw(float yawDelta) {
		setYaw(getYaw() + yawDelta);
	}

	public void addRoll(float rollDelta) {
		setRoll(getRoll() + rollDelta);
	}


	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public float getPitch() {
		return rotation.x;
	}

	public float getYaw() {
		return rotation.y;
	}

	public float getRoll() {
		return rotation.z;
	}

	//	public Vector3f getRotation() {
	//		return rotation;
	//	}


	public abstract void update();
}
