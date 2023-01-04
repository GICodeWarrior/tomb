/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui.detail;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;

import net.gicode.tomb.entry.PasswordEntry;
import net.gicode.tomb.ui.TombFont;
import net.gicode.tomb.ui.TombGUI;
import net.gicode.tomb.ui.icon.TombIcons;
import net.miginfocom.swing.MigLayout;

public class PasswordPanel extends DetailPanel {
	private static Timer clipboardEraseTimer = null; // Singleton

	private PasswordEntry password;

	private JTextField txtName;
	private JTextArea txtrDescription;
	private JTextField txtUserName;
	private JPasswordField txtPassword;

	/**
	 * Create the panel.
	 */
	protected PasswordPanel(PasswordEntry password) {
		this.password = password;
		setLayout(new MigLayout("", "[grow,shrink 1,fill][]", "[][][][50px][][][][]"));

		JLabel lblName = new JLabel("Name");
		add(lblName, "cell 0 0");

		txtName = new JTextField();
		txtName.setText(password.getName());
		lblName.setLabelFor(txtName);
		add(txtName, "cell 0 1 2 1");
		assignListener(txtName);

		JLabel lblDescription = new JLabel("Description");
		add(lblDescription, "cell 0 2");

		txtrDescription = new JTextArea();
		txtrDescription.setText(password.getDescription());
		txtrDescription.setWrapStyleWord(true);
		txtrDescription.setLineWrap(true);
		txtrDescription.setFont(UIManager.getFont("TextField.font"));
		txtrDescription.setBorder(UIManager.getBorder("TextField.border"));
		txtrDescription.setMargin(new Insets(0, 0, 12, 0));
		lblDescription.setLabelFor(txtrDescription);
		assignListener(txtrDescription);
		add(txtrDescription, "cell 0 3 2 1,wmin 10,growy");

		JLabel lblUserName = new JLabel("User name");
		add(lblUserName, "cell 0 4");

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		JButton btnCopyUserName = new JButton(TombIcons.getIcon(TombIcons.EDIT_COPY));
		btnCopyUserName.setToolTipText("Copy user name to clipboard.");
		btnCopyUserName.setMargin(new Insets(0, 0, 0, 0));
		btnCopyUserName.setBorderPainted(false);
		btnCopyUserName.setContentAreaFilled(false);
		btnCopyUserName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				StringSelection selection = new StringSelection(txtUserName.getText());
				clipboard.setContents(selection, selection);
			}
		});
		add(btnCopyUserName, "cell 1 4 1 2,aligny top");

		txtUserName = new JTextField();
		txtUserName.setText(password.getUsername());
		lblUserName.setLabelFor(txtUserName);
		add(txtUserName, "cell 0 5 2 1");
		assignListener(txtUserName);

		JLabel lblPassword = new JLabel("Password");
		add(lblPassword, "cell 0 6");

		JButton btnCopyPassword = new JButton();
		btnCopyPassword.setToolTipText("Copy password to clipboard.");
		btnCopyPassword.setMargin(new Insets(0, 0, 0, 0));
		btnCopyPassword.setIcon(TombIcons.getIcon(TombIcons.EDIT_COPY));
		btnCopyPassword.setBorderPainted(false);
		btnCopyPassword.setContentAreaFilled(false);
		btnCopyPassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String password = new String(txtPassword.getPassword());
				StringSelection selection = new StringSelection(password);

				clipboard.setContents(selection, selection);

				if (clipboardEraseTimer != null) {
					clipboardEraseTimer.stop();
				}

				clipboardEraseTimer = new Timer(TombGUI.CLIPBOARD_ERASE_PASSWORD_TIMEOUT, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						Transferable clipboardContents = clipboard.getContents(null);
						try {
							if ((clipboardContents != null)
									&& clipboardContents.isDataFlavorSupported(DataFlavor.stringFlavor)
									&& password.equals(clipboardContents.getTransferData(DataFlavor.stringFlavor))) {
								StringSelection selection = new StringSelection("");
								clipboard.setContents(selection, selection);
							}
						} catch (UnsupportedFlavorException | IOException e) {
							System.err.println("TombGUI: Error clearing password from clipboard.");
							e.printStackTrace();
						}

						// Clear the reference to this Timer so it can be GC'd
						clipboardEraseTimer = null;
					}
				});
				clipboardEraseTimer.start();
			}
		});
		add(btnCopyPassword, "cell 1 6 1 2,aligny top");

		txtPassword = new JPasswordField();
		txtPassword.setText(password.getPassword());
		txtPassword.setFont(TombFont.getMonospaced(Font.PLAIN, 12));
		final char DEFAULT_ECHO_CHAR = txtPassword.getEchoChar();
		txtPassword.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				txtPassword.setEchoChar((char) 0);
			}

			@Override
			public void focusLost(FocusEvent e) {
				txtPassword.setEchoChar(DEFAULT_ECHO_CHAR);
			}
		});
		lblPassword.setLabelFor(txtPassword);
		add(txtPassword, "cell 0 7 2 1");
		assignListener(txtPassword);
	}

	@Override
	public void focusAsNew() {
		txtName.requestFocus();
		txtName.selectAll();
	}

	@Override
	protected void updateModel() {
		password.setName(txtName.getText());
		password.setDescription(txtrDescription.getText());
		password.setUsername(txtUserName.getText());
		password.setPassword(new String(txtPassword.getPassword()));
	}
}
