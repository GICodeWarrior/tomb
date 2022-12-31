/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui.tree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.gicode.tomb.entry.Entry;

public class TombTreeCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object entry, boolean isSelected, boolean isExpanded,
			boolean isLeaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, entry, isSelected, isExpanded, isLeaf, row, hasFocus);

		if (entry instanceof Entry) {
			setText(((Entry) entry).getName());
		}

		return this;
	}
}
