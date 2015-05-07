package particleVisualization.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public abstract class AbstractBboxDrawable extends Drawable {


	final Vector3f bboxMin;
	final Vector3f bboxMax;
	boolean drawBbox = false;
	boolean drawAxes = false;


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

	@Override
	public void draw() {
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
		if (drawBbox) {
			drawBbox();
		}

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		//enable Lightning ?
		drawGeometry();
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
