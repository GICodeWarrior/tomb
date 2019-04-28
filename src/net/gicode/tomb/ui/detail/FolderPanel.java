/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui.detail;

import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import net.gicode.tomb.entry.FolderEntry;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class FolderPanel extends DetailPanel {

	private FolderEntry folder;

	private JTextField txtName;
	private JTextArea txtrDescription;

	/**
	 * Create the panel.
	 */
	protected FolderPanel(FolderEntry folder) {
		this.folder = folder;
		setLayout(new MigLayout("", "[grow,shrink 99,fill]", "[][][][100px]"));

		JLabel lblName = new JLabel("Name");
		add(lblName, "cell 0 0");

		txtName = new JTextField();
		txtName.setText(folder.getName());
		lblName.setLabelFor(txtName);
		add(txtName, "cell 0 1");
		assignListener(txtName);

		JLabel lblDescription = new JLabel("Description");
		add(lblDescription, "cell 0 2");

		txtrDescription = new JTextArea();
		txtrDescription.setText(folder.getDescription());
		txtrDescription.setWrapStyleWord(true);
		txtrDescription.setLineWrap(true);
		txtrDescription.setFont(UIManager.getFont("TextField.font"));
		txtrDescription.setBorder(UIManager.getBorder("TextField.border"));
		txtrDescription.setMargin(new Insets(0, 0, 12, 0));
		lblDescription.setLabelFor(txtrDescription);
		assignListener(txtrDescription);
		add(txtrDescription, "cell 0 3,wmin 10,growy");
	}

	@Override
	protected void updateModel() {
		folder.setName(txtName.getText());
		folder.setDescription(txtrDescription.getText());
	}
}
