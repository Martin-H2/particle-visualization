package particleVisualization.model;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.rendering.HeadUpDisplay;
import particleVisualization.rendering.SimpleObjectViewer;
import particleVisualization.util.MiscUtils;

public class Camera extends Entity {

	Matrix4f			projectionMatrix	= new Matrix4f();
	Matrix4f			viewMatrix			= new Matrix4f();
	Vector3f			viewVector			= new Vector3f();
	private final float	mouseSensitivity	= 0.15f;
	private final float	maxPitch			= 80;


	public Camera(float windowWidth, float windowHeight, float fieldOfView) {
		float aspectRatio = windowWidth / windowHeight;
		float near_plane = 0.1f;
		float far_plane = 300f;
		float y_scale = MiscUtils.coTangent(MiscUtils.degreesToRadians(fieldOfView / 2f));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = far_plane - near_plane;
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -(2 * near_plane * far_plane / frustum_length);
		projectionMatrix.m33 = 0;
	}



	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}



	@Override
	public void update() {
		if (needsMatrixUpdate) {
			updateViewMatrix();
			updateViewVector();
			needsMatrixUpdate = false;
		}
		float translationStep = SimpleObjectViewer.getFrameTimeMs() / 200.0f;
		float rotationStep = SimpleObjectViewer.getFrameTimeMs() / 10.0f;


		if (InputManager.isKeyDown(GLFW.GLFW_KEY_W)) {
			translate(viewVector, -translationStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_S)) {
			translate(viewVector, translationStep);
		}

		if (InputManager.isKeyDown(GLFW.GLFW_KEY_D)) {
			Vector3f rightVector = Vector3f.cross(viewVector, UNIT_VECTOR_Y, null);
			rightVector.scale(translationStep);
			translate(rightVector);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_A)) {
			Vector3f leftVector = Vector3f.cross(UNIT_VECTOR_Y, viewVector, null);
			leftVector.scale(translationStep);
			translate(leftVector);
		}

		if (InputManager.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
			translate(0, translationStep, 0);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
			translate(0, -translationStep, 0);
		}

		if (InputManager.isKeyDown(GLFW.GLFW_KEY_F2)) {
			setFrontViewPos();
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_F3)) {
			setScreenShotPos();
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_F4)) {
			setTrailZoomPos();
		}


		if (InputManager.isLockedOnRightMouse()) {
			addPitchClipped(InputManager.pollMouseYd() * mouseSensitivity, maxPitch);
			addYaw(InputManager.pollMouseXd() * mouseSensitivity);
		}


		HeadUpDisplay.putDebugValue(HudDebugKeys.camPos, MiscUtils.formatVec3(getPosition()));
		HeadUpDisplay.putDebugValue(HudDebugKeys.camRot, MiscUtils.formatVec3(getRotation()));

	}


	public void setScreenShotPos() {
		setPitch(31);
		setYaw(-47);
		setRoll(0);
		setPosition(4, 2.5f, -3);
	}

	public void setFrontViewPos() {
		setPitch(0);
		setYaw(0);
		setRoll(0);
		setPosition(0, 0, -2);
	}

	public void setTrailZoomPos() {
		setPitch(-6);
		setYaw(-5);
		setRoll(0);
		setPosition(0, 0, -0.6f);
	}



	private void updateViewMatrix() {
		viewMatrix.setIdentity();
		Matrix4f.rotate(MiscUtils.degreesToRadians(getPitch()), UNIT_VECTOR_X, viewMatrix, viewMatrix);
		Matrix4f.rotate(MiscUtils.degreesToRadians(getYaw()), UNIT_VECTOR_Y, viewMatrix, viewMatrix);
		Matrix4f.rotate(MiscUtils.degreesToRadians(getRoll()), UNIT_VECTOR_Z, viewMatrix, viewMatrix);
		Matrix4f.scale(new Vector3f(1, 1, -1), viewMatrix, viewMatrix);
		Matrix4f.translate(getPosition().negate(null), viewMatrix, viewMatrix);
	}

	private void updateViewVector() {
		viewVector.setX(viewMatrix.m20);
		viewVector.setY(0);
		viewVector.setZ(viewMatrix.m22);
		viewVector.normalise();
	}



}
