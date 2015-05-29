package particleVisualization.rendering;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import particleVisualization.util.MiscUtils;

public class Texture {

	public static final String	TEXTURE_FOLDER	= "res/images/";

	private int					width, height;
	private final int			textureId;

	public Texture(String path) {
		textureId = loadFromFile(TEXTURE_FOLDER + path);
	}


	public void bind() {
		glBindTexture(GL_TEXTURE_2D, textureId);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void destroy() {
		GL11.glDeleteTextures(textureId);
	}



	private int loadFromFile(String path) { //TODO better solution ?
		int[] pixels = null;
		try {
			InputStream in = new FileInputStream(path);
			BufferedImage image = ImageIO.read(in);
			width = image.getWidth();
			height = image.getHeight();
			pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		int[] data = new int[width * height];
		for (int i = 0; i < width * height; i++) {
			int a = (pixels[i] & 0xff000000) >> 24;
		int r = (pixels[i] & 0xff0000) >> 16;
		int g = (pixels[i] & 0xff00) >> 8;
		int b = pixels[i] & 0xff;
		data[i] = a << 24 | b << 16 | g << 8 | r;
		}

		int result = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, result);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, MiscUtils.createIntBuffer(data));
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		GL30.glGenerateMipmap(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, 0);
		return result;
	}

}
