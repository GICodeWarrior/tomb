/*
 * Copyright (c) 2016 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui.tree;

import java.util.LinkedHashSet;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.gicode.tomb.entry.Entry;
import net.gicode.tomb.entry.FolderEntry;
import net.gicode.tomb.entry.RootEntry;

public class TombTreeModel implements TreeModel {

	private RootEntry root;
	private LinkedHashSet<TreeModelListener> listenerSet;

	public TombTreeModel(RootEntry root) {
		this.root = root;
		listenerSet = new LinkedHashSet<TreeModelListener>();
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		listenerSet.add(listener);
	}

	@Override
	public Object getChild(Object folder, int index) {
		if (!(folder instanceof FolderEntry)) {
			return null;
		}

		return ((FolderEntry) folder).get(index);
	}

	@Override
	public int getChildCount(Object folder) {
		if (!(folder instanceof FolderEntry)) {
			return 0;
		}

		return ((FolderEntry) folder).size();
	}

	@Override
	public int getIndexOfChild(Object folder, Object entry) {
		if (!(folder instanceof FolderEntry) || !(entry instanceof Entry)) {
			return -1;
		}

		int index = 0;
		for (Entry child : (FolderEntry) folder) {
			if (child == entry) {
				return index;
			}
			index += 1;
		}

		return -1;
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object entry) {
		return !(entry instanceof FolderEntry);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener) {
		listenerSet.remove(listener);
	}

	@Override
	public void valueForPathChanged(TreePath path, Object entry) {
		TreeModelEvent event = new TreeModelEvent(this, path);

		for (TreeModelListener listener : listenerSet) {
			listener.treeNodesChanged(event);
		}
	}

	public TreePath addEntry(TreePath path, Entry entry, int index) {
		if (path == null) {
			path = new TreePath(root);
		}

		Object node = path.getLastPathComponent();
		FolderEntry folder = null;
		if (node instanceof FolderEntry) {
			folder = (FolderEntry) node;
			if (index == -1) {
				index = folder.size();
			}
		} else if (node instanceof Entry) {
			Entry selectedEntry = (Entry) node;
			path = path.getParentPath();
			node = path.getLastPathComponent();
			if (node instanceof FolderEntry) {
				folder = (FolderEntry) node;
				if (index == -1) {
					index = folder.indexOf(selectedEntry) + 1;
				}
			} else {
				System.err.println("ERROR: Invalid nodes in path during insertion.");
				return null;
			}
		}

		folder.addEntry(entry, index);
		TreeModelEvent event = new TreeModelEvent(this, path, new int[] { index }, new Object[] { entry });

		for (TreeModelListener listener : listenerSet) {
			listener.treeNodesInserted(event);
		}

		return path.pathByAddingChild(entry);
	}

	public void deleteEntry(TreePath path) {
		Entry entry = (Entry) path.getLastPathComponent();
		path = path.getParentPath();
		FolderEntry folder = (FolderEntry) path.getLastPathComponent();

		deleteEntry(path, folder.indexOf(entry));
	}

	public void deleteEntry(TreePath parentPath, int childIndex) {
		Object parent = parentPath.getLastPathComponent();
		if (!(parent instanceof FolderEntry)) {
			System.err.println("ERROR: Invalid nodes in path during deletion.");
			return;
		}

		FolderEntry folder = ((FolderEntry) parent);
		Entry entry = folder.get(childIndex);
		folder.remove(childIndex);

		TreeModelEvent event = new TreeModelEvent(this, parentPath, new int[] { childIndex }, new Object[] { entry });

		for (TreeModelListener listener : listenerSet) {
			listener.treeNodesRemoved(event);
		}
	}
}
