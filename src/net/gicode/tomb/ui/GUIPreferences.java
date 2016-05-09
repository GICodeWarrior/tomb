/*
 * Copyright (c) 2016 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui;

import java.awt.Rectangle;
import java.util.prefs.Preferences;

import sun.util.logging.PlatformLogger;
import sun.util.logging.PlatformLogger.Level;

@SuppressWarnings("restriction")
public class GUIPreferences {
	static {
		// Disable spurious warning from preferences due to static init of
		// system prefs.
		// http://bugs.java.com/bugdatabase/view_bug.do?bug_id=5097859
		// http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6809488
		PlatformLogger.getLogger("java.util.prefs").setLevel(Level.SEVERE);
	}

	private Preferences preferences;

	public GUIPreferences() {
		preferences = Preferences.userNodeForPackage(TombGUI.class);
	}

	public Rectangle readWindowBounds() {
		int x = preferences.getInt("WINDOW_BOUNDS_X", 100);
		int y = preferences.getInt("WINDOW_BOUNDS_Y", 100);
		int width = preferences.getInt("WINDOW_BOUNDS_WIDTH", 600);
		int height = preferences.getInt("WINDOW_BOUNDS_HEIGHT", 400);

		return new Rectangle(x, y, width, height);
	}

	public void storeWindowBounds(Rectangle bounds) {
		preferences.putInt("WINDOW_BOUNDS_X", bounds.x);
		preferences.putInt("WINDOW_BOUNDS_Y", bounds.y);
		preferences.putInt("WINDOW_BOUNDS_WIDTH", bounds.width);
		preferences.putInt("WINDOW_BOUNDS_HEIGHT", bounds.height);
	}

	public String readLastFileChooserPath() {
		return preferences.get("LAST_FILE_CHOOSER_PATH", null);
	}

	public void storeLastFileChooserPath(String path) {
		preferences.put("LAST_FILE_CHOOSER_PATH", path);
	}

	public int readPasswordGeneratorCharset() {
		return preferences.getInt("PASSWORD_GENERATOR_CHARSET", 0);
	}

	public void storePasswordGneratorCharset(int index) {
		preferences.putInt("PASSWORD_GENERATOR_CHARSET", index);
	}

	public int readPasswordGeneratorLength() {
		return preferences.getInt("PASSWORD_GENERATOR_LENGTH", 16);
	}

	public void storePasswordGeneratorLength(int length) {
		preferences.putInt("PASSWORD_GENERATOR_LENGTH", length);
	}
}
