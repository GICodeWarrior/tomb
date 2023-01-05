/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.ui.detail;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import net.gicode.tomb.entry.Entry;
import net.gicode.tomb.entry.FolderEntry;
import net.gicode.tomb.entry.PasswordEntry;

public abstract class DetailPanel extends JPanel implements Scrollable {

	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
			.withZone(ZoneId.systemDefault());
	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
			.withZone(ZoneId.systemDefault());

	private Entry entry;
	private ChangeListener observer;
	private UpdateListener listener = new UpdateListener();

	public static DetailPanel getPanelForEntry(Entry entry, ChangeListener observer) {
		DetailPanel panel;

		if (entry instanceof FolderEntry) {
			panel = new FolderPanel((FolderEntry) entry);
		} else if (entry instanceof PasswordEntry) {
			panel = new PasswordPanel((PasswordEntry) entry);
		} else {
			System.err.println("ERROR: Unimplemented entry type during selection change.");
			return null;
		}

		panel.entry = entry;
		panel.observer = observer;

		return panel;
	}

	protected void assignListener(JTextComponent component) {
		component.getDocument().addDocumentListener(listener);
	}

	protected String formatInstant(Instant dateTime) {
		return dateFormatter.format(dateTime) + " " + timeFormatter.format(dateTime);
	}

	public abstract void focusAsNew();

	protected abstract void updateModel();

	private void notifyObserver() {
		observer.stateChanged(new ChangeEvent(entry));
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 1;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		int parentHeight = 0;
		if (getParent() != null) {
			parentHeight = getParent().getSize().height;
		}

		if (parentHeight > size.height) {
			// Occupy at least the visible space, so components can be aligned to the bottom
			// (e.g. the created/updated info)
			size.height = parentHeight;
		}

		return size;
	}

	private class UpdateListener implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent event) {
			updateModel();
			notifyObserver();
		}

		@Override
		public void insertUpdate(DocumentEvent event) {
			updateModel();
			notifyObserver();
		}

		@Override
		public void removeUpdate(DocumentEvent event) {
			updateModel();
			notifyObserver();
		}
	}
}
