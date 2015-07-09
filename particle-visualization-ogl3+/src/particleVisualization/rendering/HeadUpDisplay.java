package particleVisualization.rendering;

import static org.lwjgl.opengl.GL11.*;
import java.awt.Font;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import org.lwjgl.glfw.GLFW;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import particleVisualization.control.InputManager;
import particleVisualization.enums.HudDebugKeys;


public class HeadUpDisplay {

	private final int									wWidth;
	private final int									wHeight;
	private final int									overlayLeftMargin;
	private final UnicodeFont							unicodeFont;
	private final int									lineHeight;

	private boolean										drawHud			= true;

	private final static EnumMap<HudDebugKeys, Object>	hudDebugValues	= new EnumMap<HudDebugKeys, Object>(HudDebugKeys.class);
	private final static List<String>					keyHelp			= new ArrayList<String>();
	static {
		keyHelp.add("VIEWER:");
		keyHelp.add(" g ... ground");
		keyHelp.add(" v ... vsync");
		keyHelp.add(" wsad space shift");
		keyHelp.add(" rMouse ... look");
		keyHelp.add(" ");
		keyHelp.add("PARTICLES:");
		keyHelp.add(" e ... expand");
		keyHelp.add(" q ... shrink");
		keyHelp.add(" f ... longer trail");
		keyHelp.add(" c ... shorter trail");
		keyHelp.add(" x ... faster");
		keyHelp.add(" y ... slower");
		keyHelp.add(" num+ ... more p.");
		keyHelp.add(" num- ... less p.");
		keyHelp.add(" TAB  play/pause");
		keyHelp.add(" lMouse ... turn");
	}

	@SuppressWarnings ("unchecked")
	public HeadUpDisplay(int windowWidth, int windowHeight) {

		wWidth = windowWidth;
		wHeight = windowHeight;
		overlayLeftMargin = wWidth - 110;

		//unicode font
		Font f = new Font("Arial", Font.PLAIN, 12);
		unicodeFont = new UnicodeFont(f);
		unicodeFont.addAsciiGlyphs();
		unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.LIGHT_GRAY));
		try {
			unicodeFont.loadGlyphs();
		}
		catch (SlickException e) {
			e.printStackTrace();
		}
		lineHeight = unicodeFont.getLineHeight();
	}

	public static void putDebugValue(HudDebugKeys key, Object value) {
		hudDebugValues.put(key, value);
	}

	public static void removeDebugValue(HudDebugKeys key) {
		hudDebugValues.remove(key);
	}

	public void update() {
		if (InputManager.isKeyDownEvent(GLFW.GLFW_KEY_F1)) {
			drawHud = !drawHud;
		}
	}

	public void draw() {
		//TODO USE MODERN WAY TO RENDER HUD (signed distance field font + shader)

		if (!drawHud) return;

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
		glColor4f(0.1f, 0.1f, 0.1f, 0.6f);
		glVertex2f(overlayLeftMargin, 0);
		glVertex2f(overlayLeftMargin, hudDebugValues.size() * lineHeight + 10);
		glVertex2f(wWidth, hudDebugValues.size() * lineHeight + 10);
		glVertex2f(wWidth, 0);
		glEnd();
		float y = 5;
		for (Entry<HudDebugKeys, Object> entry: hudDebugValues.entrySet()) {
			unicodeFont.drawString(overlayLeftMargin + 5, y, entry.getKey() + ": " + entry.getValue());
			y += lineHeight;
		}
		glBindTexture(GL_TEXTURE_2D, 0);

		y = wHeight - (keyHelp.size() * lineHeight + 15);
		glBegin(GL_QUADS);
		glColor4f(0.1f, 0.1f, 0.1f, 0.6f);
		glVertex2f(overlayLeftMargin, y);
		glVertex2f(overlayLeftMargin, wHeight);
		glVertex2f(wWidth, wHeight);
		glVertex2f(wWidth, y);
		glEnd();
		y += 5;
		for (String s: keyHelp) {
			unicodeFont.drawString(overlayLeftMargin + 5, y, s);
			y += lineHeight;
		}
		glBindTexture(GL_TEXTURE_2D, 0);

		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();

	}

}
