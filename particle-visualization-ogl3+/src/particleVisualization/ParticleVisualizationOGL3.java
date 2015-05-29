package particleVisualization;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import particleVisualization.model.MmpldData;
import particleVisualization.rendering.SimpleObjectViewer;

public class ParticleVisualizationOGL3 {

	public static void main(String[] args) {


		if (args.length<1) {
			System.err.println(".mmpld file needed as parameter");
			return;
		}

		try {
			MmpldData particleData = MmpldData.parseFrom(new File(args[0]));
			SimpleObjectViewer viewer = new SimpleObjectViewer(1200, 720, "Particle Visualization OGL3");

			viewer.setup(particleData);
			viewer.run();
			viewer.cleanup();

		} catch (DataFormatException | IOException | InterruptedException e) {
			e.printStackTrace();
		}




	}

}
