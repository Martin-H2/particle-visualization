package particleVisualization.rendering;

import static org.lwjgl.opengl.GL11.*;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map.Entry;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import particleVisualization.enums.HudDebugKeys;


public class HeadUpDisplay {

	private final int									wWidth;
	private final int									wHeight;
	private final int									overlayLeftMargin;
	private static int									overlayBottomMargin	= 20;
	private final UnicodeFont							unicodeFont;

	private final static EnumMap<HudDebugKeys, Object>	hudDebugValues		= new EnumMap<HudDebugKeys, Object>(HudDebugKeys.class);


	@SuppressWarnings ("unchecked")
	public HeadUpDisplay(int windowWidth, int windowHeight) {

		wWidth = windowWidth;
		wHeight = windowHeight;
		overlayLeftMargin = wWidth - 130;

		//unicode font
		Font f = new Font("Arial", Font.PLAIN, 14);
		unicodeFont = new UnicodeFont(f);
		unicodeFont.addAsciiGlyphs();
		unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
		try {
			unicodeFont.loadGlyphs();
		}
		catch (SlickException e) {
			e.printStackTrace();
		}

	}

	public static void putDebugValue(HudDebugKeys key, Object value) {
		hudDebugValues.put(key, value);
		overlayBottomMargin = hudDebugValues.size() * 15 + 20;
	}

	public static void removeDebugValue(HudDebugKeys key) {
		hudDebugValues.remove(key);
		overlayBottomMargin = hudDebugValues.size() * 15 + 20;
	}



	public void draw() {
		//TODO USE MODERN WAY TO RENDER HUD (signed distance field font + shader)

		glDisable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrtho(0, wWidth, wHeight, 0, 1, -1);

		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glLoadIdentity();

		glBegin(GL_QUADS);
		glColor4f(0.3f, 0.3f, 0.3f, 0.5f);
		glVertex2f(overlayLeftMargin, 0);
		glVertex2f(overlayLeftMargin, overlayBottomMargin);
		glVertex2f(wWidth, overlayBottomMargin);
		glVertex2f(wWidth, 0);
		glEnd();

		float y = 10;
		for (Entry<HudDebugKeys, Object> entry: hudDebugValues.entrySet()) {
			unicodeFont.drawString(overlayLeftMargin + 5, y, entry.getKey() + ": " + entry.getValue());
			y += 15;
		}
		glBindTexture(GL_TEXTURE_2D, 0);

		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();

	}

}
