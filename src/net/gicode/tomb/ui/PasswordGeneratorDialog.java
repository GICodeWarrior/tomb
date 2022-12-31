/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.gicode.tomb.RandomPassword;
import net.gicode.tomb.ui.icon.TombIcons;
import net.miginfocom.swing.MigLayout;

public class PasswordGeneratorDialog extends JPanel {
	private static final int FIELDS = 10;
	private static final int COLUMNS = 2;

	private JDialog dialog;
	private JComboBox<ComboItem> charsetCombo;
	private JTextField txtLength;
	private JTextField[] txtsPassword = new JTextField[FIELDS];
	private JButton[] btnsCopy = new JButton[FIELDS];

	private CopyListener listener = new CopyListener();
	private GUIPreferences preferences;

	/**
	 * Create the panel.
	 */
	public PasswordGeneratorDialog(JFrame owner, GUIPreferences preferences) {
		this.preferences = preferences;
		setLayout(new MigLayout("", "[grow,shrink 1][][grow,shrink 1][grow 1][]", "[][][][][][][][]"));

		JLabel lblCharacters = new JLabel("Characters");
		add(lblCharacters, "cell 0 0 2 1");

		JLabel lblLength = new JLabel("Length");
		add(lblLength, "cell 2 0");

		charsetCombo = new JComboBox<ComboItem>();
		charsetCombo.addItem(new ComboItem("Numbers & Letters", RandomPassword.ALPHA_NUMERIC));
		charsetCombo.addItem(new ComboItem("Letters", RandomPassword.ALPHAS));
		charsetCombo.addItem(new ComboItem("Numbers", RandomPassword.NUMBERS));
		charsetCombo.setSelectedIndex(preferences.readPasswordGeneratorCharset());
		lblCharacters.setLabelFor(charsetCombo);
		add(charsetCombo, "cell 0 1 2 1,growx");

		txtLength = new JTextField();
		lblLength.setLabelFor(txtLength);
		txtLength.setText("" + preferences.readPasswordGeneratorLength());
		txtLength.setColumns(4);
		add(txtLength, "cell 2 1,growx");
		txtLength.setColumns(10);

		JButton btnGenerate = new JButton("Generate");
		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				regenerate();
			}
		});
		add(btnGenerate, "cell 3 1 2 1");

		int passwordRows = FIELDS / COLUMNS;
		for (int c = 0; c < FIELDS; ++c) {
			int row = c % passwordRows + 3;
			int column = c / passwordRows;

			txtsPassword[c] = new JTextField();
			txtsPassword[c].setEditable(false);
			txtsPassword[c].setColumns(20);
			txtsPassword[c].setFont(new Font("Monospaced", Font.PLAIN, 12));
			add(txtsPassword[c], "cell " + column * 2 + " " + row + " " + (column + 1) + " 1,growx");

			btnsCopy[c] = new JButton(TombIcons.getIcon(TombIcons.EDIT_COPY));
			btnsCopy[c].setMargin(new Insets(0, 0, 0, 0));
			btnsCopy[c].setBorderPainted(false);
			btnsCopy[c].setContentAreaFilled(false);
			btnsCopy[c].addActionListener(listener);
			add(btnsCopy[c], "cell " + (column * 3 + 1) + " " + row);
		}

		dialog = new JDialog(owner, "Password Generator", false);
		dialog.setContentPane(this);
		dialog.pack();
	}

	public void showDialog() {
		regenerate();

		dialog.setLocationRelativeTo(dialog.getOwner());
		dialog.setVisible(true);
	}

	private void regenerate() {
		int length = 16;
		try {
			length = Integer.parseInt(txtLength.getText());
		} catch (NumberFormatException e) {
			System.err.println("WARNING: Invalid length specified for password generation.");
		}

		int index = charsetCombo.getSelectedIndex();
		ComboItem item = charsetCombo.getItemAt(index);

		preferences.storePasswordGeneratorCharset(index);
		preferences.storePasswordGeneratorLength(length);

		for (JTextField passwordField : txtsPassword) {
			passwordField.setText(RandomPassword.generate(length, item.charset));
		}
	}

	private class ComboItem {
		private String name;
		private String charset;

		private ComboItem(String name, String charset) {
			this.name = name;
			this.charset = charset;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private class CopyListener implements ActionListener {
		private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		@Override
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			String password = "";
			for (int c = 0; c < FIELDS; ++c) {
				if (source == btnsCopy[c]) {
					password = txtsPassword[c].getText();
					break;
				}
			}

			StringSelection selection = new StringSelection(password);
			clipboard.setContents(selection, selection);
		}
	}
}
