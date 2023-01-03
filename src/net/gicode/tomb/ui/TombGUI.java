/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.Instant;
import java.util.regex.Matcher;

import javax.swing.Box;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.gicode.tomb.TombFile;
import net.gicode.tomb.entry.Entry;
import net.gicode.tomb.entry.FolderEntry;
import net.gicode.tomb.entry.PasswordEntry;
import net.gicode.tomb.ui.detail.DetailPanel;
import net.gicode.tomb.ui.icon.TombIcons;
import net.gicode.tomb.ui.tree.EntryTransferHandler;
import net.gicode.tomb.ui.tree.TombTreeCellRenderer;
import net.gicode.tomb.ui.tree.TombTreeModel;

public class TombGUI {
	public static final int CLIPBOARD_ERASE_PASSWORD_TIMEOUT = 120 * 1000; // milliseconds

	private static final String WINDOW_TITLE = "%s - Tomb Password Manager";

	private JFrame tombFrame;

	private JTree tree;
	private TombTreeModel treeModel;
	private JScrollPane contentScrollPane;
	private JFileChooser chooser;
	private PasswordGeneratorDialog generator = null;

	private TombFile tombFile = new TombFile();
	private Instant lastSaved = Instant.now();
	private String location = null;
	private String password = null;
	private boolean titleDirty = false;

	private int newFolderId = 1;
	private int newPasswordId = 1;

	private GUIPreferences preferences = new GUIPreferences();

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				TombGUI window = new TombGUI();
				window.tombFrame.setVisible(true);

				if (args.length == 1) {
					window.loadTombFile(args[0]);
				}
			}
		});
	}

	public TombGUI() {
		initialize();
	}

	private void initialize() {
		tombFrame = new JFrame();
		tombFrame.setBounds(preferences.readWindowBounds());
		tombFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		tombFrame.setIconImages(TombIcons.getApplicationIcons());

		tombFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		tombFrame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tombFile.getUpdated().isAfter(lastSaved)) {
					int response = JOptionPane.showConfirmDialog(tombFrame,
							"Your changes will be lost.  Create a fresh tomb anyways?", "Are you sure?",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

					if (response != JOptionPane.OK_OPTION) {
						return;
					}
				}

				tombFile = new TombFile();
				location = null;
				password = null;
				lastSaved = Instant.now();

				treeModel = new TombTreeModel(tombFile.getRoot());
				tree.setModel(treeModel);
				tree.setTransferHandler(new EntryTransferHandler(treeModel));

				updateTitle();
			}
		});
		mnFile.add(mntmNew);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (tombFile.getUpdated().isAfter(lastSaved)) {
					int response = JOptionPane.showConfirmDialog(tombFrame,
							"Your changes will be lost.  Open another tomb anyways?", "Unsaved Changes",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

					if (response != JOptionPane.OK_OPTION) {
						return;
					}
				}
				int returnVal = chooser.showOpenDialog(tombFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File passwordTomb = chooser.getSelectedFile();
					loadTombFile(passwordTomb.getPath());
				}
			}
		});
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				saveTomb(false);
			}
		});
		mnFile.add(mntmSave);

		JMenuItem mntmSaveAs = new JMenuItem("Save as...");
		mntmSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				saveTomb(true);
			}
		});
		mnFile.add(mntmSaveAs);

		JMenuItem mntmChangePassword = new JMenuItem("Set unlock password");
		mntmChangePassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SetPasswordDialog passwordDialog = new SetPasswordDialog();
				String newPassword = passwordDialog.getPassword(tombFrame);
				if (newPassword != null) {
					password = newPassword;
					JOptionPane.showMessageDialog(tombFrame, "New password will be used duing next Save.",
							"Password Updated", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		mnFile.add(mntmChangePassword);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		mnFile.add(mntmQuit);

		JLabel lblTombVersion = new JLabel("v" + TombFile.SOFTWARE_VERSION);
		lblTombVersion.setBorder(new EmptyBorder(0, 0, 0, 5));

		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		JMenuItem mntmPasswordGenerator = new JMenuItem("Password Generator");
		mntmPasswordGenerator.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (generator == null) {
					generator = new PasswordGeneratorDialog(tombFrame, preferences);
				}
				generator.showDialog();
			}
		});
		mnTools.add(mntmPasswordGenerator);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(lblTombVersion);

		JSplitPane splitPane = new JSplitPane();
		tombFrame.getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel navigationPanel = new JPanel();
		splitPane.setLeftComponent(navigationPanel);
		navigationPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		navigationPanel.add(scrollPane, BorderLayout.CENTER);

		// Hide leaf icons in tree
		UIManager.put("Tree.leafIcon", new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
			}

			@Override
			public int getIconWidth() {
				return 0;
			}

			@Override
			public int getIconHeight() {
				return 20;
			}
		});

		tree = new JTree();
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent event) {
				TreePath path = event.getNewLeadSelectionPath();
				if (path == null) {
					contentScrollPane.setViewportView(null);
					return;
				}

				Object node = path.getLastPathComponent();
				if (!(node instanceof Entry)) {
					contentScrollPane.setViewportView(null);
					System.err.println("ERROR: Invalid node after selection change.");
					return;
				}

				// Listen for updates on the detail panel and update the name in the tree
				ChangeListener listener = new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent event) {
						treeModel.valueForPathChanged(tree.getSelectionPath(), event.getSource());
						updateDirty();
					}
				};
				contentScrollPane.setViewportView(DetailPanel.getPanelForEntry((Entry) node, listener));
			}
		});
		initializeTreeModel();
		tree.setCellRenderer(new TombTreeCellRenderer());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.INSERT);
		scrollPane.setViewportView(tree);

		JPanel actionPanel = new JPanel();
		navigationPanel.add(actionPanel, BorderLayout.SOUTH);

		JButton btnAddFolder = new JButton("Add Folder");
		btnAddFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionPath();
				Entry entry = new FolderEntry("New Folder " + newFolderId++, "");
				path = treeModel.addEntry(path, entry, -1);
				tree.setSelectionPath(path);
				tree.scrollPathToVisible(path);
				updateDirty();
			}
		});
		actionPanel.add(btnAddFolder);

		JButton btnAddPassword = new JButton("Add Password");
		btnAddPassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				TreePath path = tree.getSelectionPath();
				Entry entry = new PasswordEntry("New Password " + newPasswordId++, "", "", "");
				path = treeModel.addEntry(path, entry, -1);
				tree.setSelectionPath(path);
				tree.scrollPathToVisible(path);
				updateDirty();
			}
		});
		actionPanel.add(btnAddPassword);

		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				TreePath path = tree.getSelectionPath();

				Object node = null;
				if (path != null) {
					node = path.getLastPathComponent();
				}

				if ((node == null) || (node == treeModel.getRoot()) || !(node instanceof Entry)) {
					JOptionPane.showMessageDialog(tombFrame, "No entry selected.", "Unable to delete entry",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				Entry entry = (Entry) node;
				String message = "Do you want to delete " + entry.getType() + " \"" + entry.getName() + "\"?";

				int response = JOptionPane.showConfirmDialog(tombFrame, message, "Are you sure?",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (response == JOptionPane.OK_OPTION) {
					treeModel.deleteEntry(path);
				}
				updateDirty();
			}
		});
		actionPanel.add(btnDelete);

		contentScrollPane = new JScrollPane();
		splitPane.setRightComponent(contentScrollPane);

		chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Password Tomb", "tomb");
		chooser.setFileFilter(filter);
		String path = preferences.readLastFileChooserPath();
		if (path != null) {
			chooser.setCurrentDirectory(new File(path));
		}

		updateTitle();
	}

	private void loadTombFile(String path) {
		UnlockDialog unlockDialog = new UnlockDialog(path);
		String unlockPassword = unlockDialog.getPassword(tombFrame);
		if (unlockPassword == null) {
			return;
		}

		RunWithProgressDialog dialog = new RunWithProgressDialog(tombFrame, "Unable to open Tomb");
		boolean success = dialog.execute(() -> tombFile.load(path, unlockPassword));

		if (success) {
			location = path;
			password = unlockPassword;
			lastSaved = Instant.now();

			initializeTreeModel();
			updateTitle();
		}
	}

	private boolean saveTomb(boolean saveAs) {
		if (password == null) {
			SetPasswordDialog passwordDialog = new SetPasswordDialog();
			String newPassword = passwordDialog.getPassword(tombFrame);
			if (newPassword == null) {
				JOptionPane.showMessageDialog(tombFrame, "Unable to save without password.", "Missing Password",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			password = newPassword;
		}

		String saveLocation = location;
		if (saveAs || (saveLocation == null)) {
			int returnVal = chooser.showSaveDialog(tombFrame);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return false;
			}
			saveLocation = chooser.getSelectedFile().getPath();
		}
		// Necessary for reference in lambda
		final String finalSaveLocation = saveLocation;

		RunWithProgressDialog dialog = new RunWithProgressDialog(tombFrame, "Error Saving File");
		boolean success = dialog.execute(() -> tombFile.save(finalSaveLocation, password));

		if (success) {
			location = saveLocation;
			lastSaved = Instant.now();
			updateTitle();
		}

		return success;
	}

	private void quit() {
		if (tombFile.getUpdated().isAfter(lastSaved)) {
			int response = JOptionPane.showConfirmDialog(tombFrame, "Would you like to save changes before exiting?",
					"Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if ((response == JOptionPane.CANCEL_OPTION) || ((response == JOptionPane.YES_OPTION) && !saveTomb(false))) {
				return;
			}
		}

		preferences.storeWindowBounds(tombFrame.getBounds());
		preferences.storeLastFileChooserPath(chooser.getCurrentDirectory().getPath());

		System.exit(0);
	}

	private void updateTitle() {
		updateTitle(true);
	}

	private void updateDirty() {
		updateTitle(false);
	}

	private void updateTitle(boolean force) {
		if (!force && titleDirty) {
			return;
		}

		String fileName = "New Tomb";
		if (location != null) {
			fileName = location.replaceFirst("^.*" + Matcher.quoteReplacement(File.separator), "");
		}

		titleDirty = false;
		if (tombFile.getUpdated().isAfter(lastSaved)) {
			fileName = "*" + fileName;
			titleDirty = true;
		}

		tombFrame.setTitle(String.format(WINDOW_TITLE, fileName));
	}

	private void initializeTreeModel() {
		treeModel = new TombTreeModel(tombFile.getRoot());
		treeModel.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				updateDirty();
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				updateDirty();
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				updateDirty();
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				updateDirty();
			}
		});

		tree.setModel(treeModel);
		tree.setTransferHandler(new EntryTransferHandler(treeModel));
	}
}
