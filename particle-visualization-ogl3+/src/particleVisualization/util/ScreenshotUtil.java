package particleVisualization.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import particleVisualization.rendering.SimpleObjectViewer;


public class ScreenshotUtil {

	private static final String	SCREENSHOT_DIR	= "./screenshots/";



	public static void savePngScreenShot() {
		new File(SCREENSHOT_DIR).mkdirs();
		String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
		savePngScreenShot(SCREENSHOT_DIR + date + ".png");
	}

	public static void savePngScreenShot(String path) {
		GL11.glReadBuffer(GL11.GL_FRONT);
		int width = SimpleObjectViewer.windowWidth;
		int height = SimpleObjectViewer.windowHeight;
		int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int i = (x + width * y) * bpp;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				image.setRGB(x, height - (y + 1), 0xFF << 24 | r << 16 | g << 8 | b);
			}
		}
		try {
			ImageIO.write(image, "PNG", new File(path));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
