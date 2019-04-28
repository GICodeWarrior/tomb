/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public class SetPasswordDialog extends JPanel {
	private JPasswordField setPasswordField;
	private JPasswordField confirmPasswordField;

	/**
	 * Create the panel.
	 */
	public SetPasswordDialog() {
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel nestedPanel = new JPanel();
		add(nestedPanel);
		nestedPanel.setLayout(new BoxLayout(nestedPanel, BoxLayout.Y_AXIS));

		JLabel lblSetPasswordLabel = new JLabel("Set unlock password");
		nestedPanel.add(lblSetPasswordLabel);

		setPasswordField = new JPasswordField();
		lblSetPasswordLabel.setLabelFor(setPasswordField);
		nestedPanel.add(setPasswordField);
		setPasswordField.setColumns(32);

		JLabel lblConfirmPasswordLabel = new JLabel("Confirm password");
		nestedPanel.add(lblConfirmPasswordLabel);

		confirmPasswordField = new JPasswordField();
		lblConfirmPasswordLabel.setLabelFor(confirmPasswordField);
		nestedPanel.add(confirmPasswordField);
		confirmPasswordField.setColumns(10);

		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				setPasswordField.requestFocus();
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
		while (true) {
			int result = JOptionPane.showConfirmDialog(window, this, "Set Unlock Password",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			String password = new String(setPasswordField.getPassword());
			String confirmation = new String(confirmPasswordField.getPassword());
			if (result == JOptionPane.OK_OPTION) {
				if (!password.equals(confirmation)) {
					JOptionPane.showMessageDialog(window, "Password and confirmation don't match.", "Invalid Password",
							JOptionPane.ERROR_MESSAGE);
					continue;
				}
				return password;
			}

			return null;
		}
	}
}
