package particleVisualization.model;

import java.util.List;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.vector.Vector4f;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;
import particleVisualization.enums.RenderMode;
import particleVisualization.rendering.HeadUpDisplay;
import particleVisualization.rendering.Shader;
import particleVisualization.rendering.SimpleObjectViewer;
import particleVisualization.rendering.Texture;
import particleVisualization.util.MiscUtils;

public class ParticleField extends DrawableEntity {

	private final List<float[]>	dataFrames;
	private int					currentFrameIndex		= 0;
	private double				currentFrameIndexD		= 0;

	private final Vector4f		globalRgba;
	private final int			maxParticlesDisplayed	= 1000;
	private final int			particlesPerFrame;
	private int					numberOfDrawnFrames		= 1;
	private boolean				paused					= true;
	private final int			dataFps					= 60;
	private int					uploadedFrames;



	public ParticleField(MmpldData particleData, Texture spriteTexture) {
		super(spriteTexture, particleData.getDataFrames().get(0), particleData.getNumberOfDataFrames() * particleData.getParticlesPerFrame(), GL11.GL_POINTS, RenderMode.textured);
		uploadedFrames = 1;
		dataFrames = particleData.getDataFrames();
		globalRgba = particleData.getGlobalRgba();
		particlesPerFrame = particleData.getParticlesPerFrame();
		setBoundingBoxMin(particleData.getBoxMin());
		setBoundingBoxMax(particleData.getBoxMax());
		drawBoundingBox(true);
		GL11.glEnable(GL20.GL_POINT_SPRITE);
		GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
	}

	@Override
	protected void setPerDrawUniforms(Shader shader) {
	}

	@Override
	protected void drawVao() {
		vertexArrayObject.draw(currentFrameIndex * particlesPerFrame, particlesPerFrame * numberOfDrawnFrames);
	}

	//	public void increaseMaxParticles(int maxParticlesInc) {
	//		maxParticlesDisplayed += maxParticlesInc;
	//		if (maxParticlesDisplayed < 0) {
	//			maxParticlesDisplayed = 1;
	//		}
	//		if (maxParticlesDisplayed > particlesPerFrame) {
	//			maxParticlesDisplayed = particlesPerFrame;
	//		}
	//	}



	@Override
	public void update() {
		//TODO send new dataFrames to GPU
		if (dataFrames.size() > uploadedFrames) {
			vertexArrayObject.appendPositionData(dataFrames.get(uploadedFrames));
			uploadedFrames++;
		}

		if (!paused) {
			currentFrameIndexD = (currentFrameIndexD + dataFps / SimpleObjectViewer.getFps()) % dataFrames.size();
			currentFrameIndex = (int) currentFrameIndexD;
		}

		float translationStep = SimpleObjectViewer.getFrameTimeMs() / 200.0f;
		float rotationStep = SimpleObjectViewer.getFrameTimeMs() / 10.0f;


		if (InputManager.isKeyDown(GLFW.GLFW_KEY_KP_ADD)) {
			numberOfDrawnFrames = MiscUtils.clip(numberOfDrawnFrames + 1, 1, uploadedFrames);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_KP_SUBTRACT)) {
			numberOfDrawnFrames = MiscUtils.clip(numberOfDrawnFrames - 1, 1, uploadedFrames);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_E)) {
			addScale(translationStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_C)) {
			addScale(-translationStep);
		}
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_TAB)) {
			paused = !paused;
		}



		//TODO mouseRot
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_PAGE_UP)) {
			//			translate(0, translationStep, 0);
			rotate(0, 0, rotationStep);
		}
		if (InputManager.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN)) {
			//			translate(0, -translationStep, 0);
			rotate(0, 0, -rotationStep);
		}



		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFps, dataFps);
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFrame, currentFrameIndex);
		HeadUpDisplay.putDebugValue(HudDebugKeys.dataFrameCount, dataFrames.size());
		HeadUpDisplay.putDebugValue(HudDebugKeys.numTailSegments, numberOfDrawnFrames - 1);
		HeadUpDisplay.putDebugValue(HudDebugKeys.numObjects, particlesPerFrame * numberOfDrawnFrames);
	}

}
