/*
 * Copyright (c) 2016 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public class UnlockDialog extends JPanel {
	private JPasswordField passwordField;

	/**
	 * Create the panel.
	 */
	public UnlockDialog(String fileName) {
		setLayout(new BorderLayout(0, 0));

		JLabel lblEnterPasswordFor = new JLabel("Enter password for " + fileName);
		add(lblEnterPasswordFor, BorderLayout.CENTER);

		passwordField = new JPasswordField();
		lblEnterPasswordFor.setLabelFor(passwordField);
		add(passwordField, BorderLayout.SOUTH);
		
		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				passwordField.requestFocus();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
		});
	}

	public String getPassword(Component window) {
		int result = JOptionPane.showConfirmDialog(window, this, "Unlock Tomb", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			return new String(passwordField.getPassword());
		}

		return null;
	}
}
