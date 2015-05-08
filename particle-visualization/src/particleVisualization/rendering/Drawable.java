package particleVisualization.rendering;

import org.lwjgl.util.vector.Vector3f;

public abstract class Drawable {

	Vector3f translation = new Vector3f();
	float roll, pitch, yaw;




	public abstract void updateModel();
	public abstract void draw();



	public void translate(float x, float y, float z) {
		translation.x += x;
		translation.y += y;
		translation.z += z;
	}

	public void addRoll(float deltaAngle) {
		roll += deltaAngle;
	}
	public void addPitch(float deltaAngle) {
		pitch += deltaAngle;
	}
	public void addYaw(float deltaAngle) {
		yaw += deltaAngle;
	}

	public void resetTransRot() {
		translation.set(0, 0, -5);
		roll = 0;
		pitch = 0;
		yaw = 0;
	}

}
