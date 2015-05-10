package particleVisualization.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public abstract class AbstractBboxDrawable extends Drawable {


	final Vector3f bboxMin;
	final Vector3f bboxMax;
	boolean drawBbox = false;
	boolean drawAxes = false;
	Vector3f translationBuf = new Vector3f();


	public AbstractBboxDrawable(Vector3f bboxMin, Vector3f bboxMax){
		this.bboxMin = bboxMin;
		this.bboxMax = bboxMax;
	}

	public void setDrawBbox(boolean drawBbox) {
		this.drawBbox = drawBbox;
	}
	public void setDrawAxes(boolean drawAxes) {
		this.drawAxes = drawAxes;
	}
	public void toggleAxes() {
		drawAxes = !drawAxes;
	}
	public void toggleBbox() {
		drawBbox = !drawBbox;
	}

	@Override
	public void draw() {
		applySmoothTranslationStep(0.2f);
		GL11.glLoadIdentity();
		GL11.glTranslatef(translation.x, translation.y, translation.z);
		// ZYX orientation method (first roll, then pitch and at the end yaw)
		GL11.glRotatef(roll, 0, 0, 1);
		GL11.glRotatef(pitch, 0, 1, 0);
		GL11.glRotatef(yaw, 1, 0, 0);

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		//disbale Lightning ?
		if (drawAxes) {
			drawAxes();
		}
		GL11.glTranslatef((bboxMin.x-bboxMax.x)/2, (bboxMin.y-bboxMax.y)/2, (bboxMin.z-bboxMax.z)/2);
		if (drawBbox) {
			drawBbox();
		}

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		//enable Lightning ?
		drawGeometry();
	}

	private void applySmoothTranslationStep(float fractionPerFrame) {
		translation.x += translationBuf.x * fractionPerFrame;
		translationBuf.x -= translationBuf.x * fractionPerFrame;
		translation.y += translationBuf.y * fractionPerFrame;
		translationBuf.y -= translationBuf.y * fractionPerFrame;
		translation.z += translationBuf.z * fractionPerFrame;
		translationBuf.z -= translationBuf.z * fractionPerFrame;
	}

	public void translateSmooth(float x, float y, float z) {
		translationBuf.x += x;
		translationBuf.y += y;
		translationBuf.z += z;
	}

	abstract public void drawGeometry();

	private void drawAxes() {
		GL11.glBegin(GL11.GL_LINES);

		GL11.glColor3f(0.6f, 0.0f, 0.0f);
		GL11.glVertex3f( 0.0f, 0.0f, 0.0f);
		GL11.glVertex3f( 1.0f, 0.0f, 0.0f);

		GL11.glColor3f(0.0f, 0.6f, 0.0f);
		GL11.glVertex3f( 0.0f, 0.0f, 0.0f);
		GL11.glVertex3f( 0.0f, 1.0f, 0.0f);

		GL11.glColor3f(0.0f, 0.0f, 1.0f);
		GL11.glVertex3f( 0.0f, 0.0f, 0.0f);
		GL11.glVertex3f( 0.0f, 0.0f, 1.0f);

		GL11.glEnd();
	}

	private void drawBbox() {
		GL11.glBegin(GL11.GL_QUAD_STRIP);

		GL11.glColor3f(0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(bboxMin.x, bboxMax.y, bboxMin.z);
		GL11.glVertex3f(bboxMin.x, bboxMin.y, bboxMin.z);
		GL11.glVertex3f(bboxMax.x, bboxMax.y, bboxMin.z);
		GL11.glVertex3f(bboxMax.x, bboxMin.y, bboxMin.z);

		GL11.glVertex3f(bboxMax.x, bboxMax.y, bboxMax.z);
		GL11.glVertex3f(bboxMax.x, bboxMin.y, bboxMax.z);
		GL11.glVertex3f(bboxMin.x, bboxMax.y, bboxMax.z);
		GL11.glVertex3f(bboxMin.x, bboxMin.y, bboxMax.z);

		GL11.glVertex3f(bboxMin.x, bboxMax.y, bboxMin.z);
		GL11.glVertex3f(bboxMin.x, bboxMin.y, bboxMin.z);

		GL11.glEnd();
	}



}
