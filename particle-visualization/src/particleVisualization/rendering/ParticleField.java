package particleVisualization.rendering;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import particleVisualization.model.MmpldData;


public class ParticleField extends AbstractBboxDrawable {

	private final List<Vector3f[]> dataFrames;
	private int currentFrameIndex = 0;

	private final Vector4f globalRgba;
	private int maxParticlesDisplayed = 1000;
	private final int maxParticlesPerFrame;

	public void increaseMaxParticles(int maxParticlesInc) {
		maxParticlesDisplayed += maxParticlesInc;
		if (maxParticlesDisplayed<0) {
			maxParticlesDisplayed=1;
		}
		if (maxParticlesDisplayed>maxParticlesPerFrame) {
			maxParticlesDisplayed=maxParticlesPerFrame;
		}
	}

	public ParticleField(MmpldData particleData) {
		super(particleData.getBoxMin(), particleData.getBoxMax());
		dataFrames = particleData.getDataFrames();
		globalRgba = particleData.getGlobalRgba();
		maxParticlesPerFrame = particleData.getMaxParticlesPerFrame();
		setDrawAxes(false);
		setDrawBbox(true);
		resetTransRot();
	}


	@Override
	public void updateModel() {
		//TODO data-frame timing
		currentFrameIndex = (currentFrameIndex+1) % dataFrames.size();
	}

	@Override
	public void drawGeometry() {
		final float r=globalRgba.getX(), g=globalRgba.getY(), b=globalRgba.getZ(), a=globalRgba.getW();
		float aFade = a-0.5f;

		if (currentFrameIndex>0) {
			GL11.glBegin(GL11.GL_LINES);
			for (int f=currentFrameIndex; f>0; f--) {
				aFade = aFade - 0.01f;
				if (aFade<=0) {
					break;
				}
				GL11.glColor4f(r,g,b,aFade);
				Vector3f[] frameIter = dataFrames.get(f);
				Vector3f[] previousFrame = dataFrames.get(f-1);
				for (int i=0; i<frameIter.length; i++) {
					Vector3f v = frameIter[i];
					if (v==null || i==maxParticlesDisplayed) {
						break;
					}
					Vector3f u = previousFrame[i];
					if (v.x>u.x && Math.abs(v.z-u.z)<0.4) {
						GL11.glVertex3f(v.x, v.y, v.z);
						GL11.glVertex3f(u.x, u.y, u.z);
					}
				}

			}
			GL11.glEnd();
		}

		GL11.glPointSize(2);
		GL11.glBegin(GL11.GL_POINTS);
		if(globalRgba!=null) {
			GL11.glColor4f(r,g,b,a);
		}
		Vector3f[] currentFrame = dataFrames.get(currentFrameIndex);
		for (int i=0; i<currentFrame.length; i++) {
			Vector3f v = currentFrame[i];
			if (v==null || i==maxParticlesDisplayed) {
				break;
			}
			GL11.glVertex3f(v.getX(), v.getY(), v.getZ());
		}
		GL11.glEnd();

		//glGetFloat(GL_POINT_SIZE);
		//glPointSize(pointSize / 1.01f);

	}

	public int getParticleCount() {
		return maxParticlesDisplayed;
	}
	public int getCurrentFrameIndex() {
		return currentFrameIndex;
	}
	public int getNumberOfFrames() {
		return dataFrames.size();
	}



}
