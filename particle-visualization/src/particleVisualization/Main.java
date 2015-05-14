package particleVisualization;

import java.io.File;

import org.lwjgl.LWJGLException;

import particleVisualization.model.MmpldData;
import particleVisualization.rendering.ParticleField;
import particleVisualization.rendering.SimpleObjectViewer;


public class Main {

	public static void main(String[] args) {

		if (args.length<1) {
			System.err.println(".mmpld file needed as parameter");
			return;
		}

		try {
			MmpldData particleData = MmpldData.parseFrom(new File(args[0]));
			ParticleField particleField = new ParticleField(particleData);
			SimpleObjectViewer viewer = new SimpleObjectViewer(1280, 720, particleField);
			particleField.setViewer(viewer);

			try {
				viewer.setup();
				viewer.mainloop();

			} catch (LWJGLException e) {
				e.printStackTrace();
			} finally {
				viewer.cleanup();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}



	}






}
