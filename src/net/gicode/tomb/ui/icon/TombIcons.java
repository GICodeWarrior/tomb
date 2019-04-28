/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui.icon;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

public class TombIcons {
	public static final String EDIT_COPY = "edit-copy";

	private static final String ROOT = "/" + TombIcons.class.getPackage().getName().replace('.', '/') + "/";
	private static final int[] APP_ICON_SIZES = new int[] { 16, 20, 32, 40, 128 };

	private static Map<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();

	public static ImageIcon getIcon(String name) {
		ImageIcon icon = iconCache.get(name);
		if (icon != null) {
			return icon;
		}

		icon = new ImageIcon(TombIcons.class.getResource(ROOT + name + ".png"));
		iconCache.put(name, icon);
		return icon;
	}

	public static List<Image> getApplicationIcons() {
		List<Image> images = new ArrayList<Image>(APP_ICON_SIZES.length);
		for (int size : APP_ICON_SIZES) {
			images.add(getIcon("tomb-icon-" + size).getImage());
		}
		return images;
	}
}
