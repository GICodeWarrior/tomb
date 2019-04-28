/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.TreePath;

import net.gicode.tomb.entry.Entry;

public class EntryTransferable implements Transferable {
	public static final DataFlavor FLAVOR = new DataFlavor(Entry.class, "Tomb Entry");

	private TreePath path;
	private int index;

	public EntryTransferable(TreePath pathToParent, int childIndex) {
		path = pathToParent;
		index = childIndex;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor == FLAVOR;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return new Object[] { path, index };
	}
}
