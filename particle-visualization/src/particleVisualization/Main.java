package particleVisualization;

import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.util.glu.GLU.gluErrorString;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;

import particleVisualization.model.MmpldData;
import particleVisualization.rendering.ParticleField;
import particleVisualization.rendering.SimpleObjectViewer;


public class Main {

	private static final String MMPLD_FILENAME = "D:/tools/MegaMol/blasen_all.mmpld";	//TODO: use program parameter etc.


	public static void main(String[] args) {

		try {
			MmpldData particleData = MmpldData.parseFrom(MMPLD_FILENAME);
			SimpleObjectViewer viewer = new SimpleObjectViewer(new ParticleField(particleData));

			try {
				viewer.setup();
				viewer.mainloop();

			} catch (LWJGLException e) {
				e.printStackTrace();
			} finally {
				if (glGetError() != GL11.GL_NO_ERROR) {
					System.err.println(gluErrorString(glGetError()));
				}
				viewer.cleanup();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}



	}






}
