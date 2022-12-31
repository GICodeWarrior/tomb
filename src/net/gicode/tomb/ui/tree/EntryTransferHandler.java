/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui.tree;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import net.gicode.tomb.entry.Entry;
import net.gicode.tomb.entry.FolderEntry;

public class EntryTransferHandler extends TransferHandler {
	private TombTreeModel treeModel;

	public EntryTransferHandler(TombTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent component) {
		if (!(component instanceof JTree)) {
			return null;
		}

		TreePath parentPath;
		int index;

		try {
			TreePath path = ((JTree) component).getSelectionPath();
			parentPath = path.getParentPath();
			FolderEntry parent = (FolderEntry) parentPath.getLastPathComponent();
			index = parent.indexOf((Entry) path.getLastPathComponent());
		} catch (ClassCastException e) {
			System.err.println("ERROR: Unexpected tree structure during drag.");
			e.printStackTrace();
			return null;
		}

		return new EntryTransferable(parentPath, index);
	}

	@Override
	public boolean canImport(TransferSupport support) {
		return support.isDataFlavorSupported(EntryTransferable.FLAVOR);
	}

	@Override
	public boolean importData(TransferSupport support) {
		Object transferData;
		try {
			transferData = support.getTransferable().getTransferData(EntryTransferable.FLAVOR);
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
			return false;
		}

		TreePath sourcePath;
		FolderEntry sourceFolder;
		int sourceIndex;

		TreePath destPath;
		FolderEntry destFolder;
		int destIndex;

		try {
			Object[] bag = (Object[]) transferData;
			sourcePath = (TreePath) bag[0];
			sourceFolder = (FolderEntry) sourcePath.getLastPathComponent();
			sourceIndex = (int) bag[1];

			JTree.DropLocation dropLocation = (javax.swing.JTree.DropLocation) support.getDropLocation();
			destPath = dropLocation.getPath();
			destFolder = (FolderEntry) destPath.getLastPathComponent();
			destIndex = dropLocation.getChildIndex();
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}

		Entry entry = sourceFolder.get(sourceIndex);

		if ((sourceFolder == destFolder) && (destIndex <= sourceIndex)) {
			treeModel.deleteEntry(sourcePath, sourceIndex);
			treeModel.addEntry(destPath, entry, destIndex);
		} else {
			treeModel.addEntry(destPath, entry, destIndex);
			treeModel.deleteEntry(sourcePath, sourceIndex);
		}

		return true;
	}
}
