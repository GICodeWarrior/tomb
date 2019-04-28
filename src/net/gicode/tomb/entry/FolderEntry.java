/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.entry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONObject;

public class FolderEntry extends Entry implements Iterable<Entry> {
	public static final String FOLDER_TYPE = "folder";

	private static final String ENTRIES_KEY = "entries";

	private ArrayList<Entry> entries = new ArrayList<Entry>();

	public FolderEntry(String name, String description) {
		super(name, description);

		data.put(ENTRIES_KEY, new JSONArray());
	}

	public FolderEntry(JSONObject data) {
		super(data);

		JSONArray rawEntries = data.getJSONArray(ENTRIES_KEY);

		for (int c = 0; c < rawEntries.length(); ++c) {
			JSONObject rawEntry = rawEntries.getJSONObject(c);
			Entry entry;

			switch (rawEntry.getString(TYPE_KEY)) {
			case FOLDER_TYPE:
				entry = new FolderEntry(rawEntry);
				break;
			case PasswordEntry.PASSWORD_TYPE:
				entry = new PasswordEntry(rawEntry);
				break;
			default:
				// XXX PANIC
				System.err.println("Unknown entry type.");
				System.exit(1);
				return;
			}

			entries.add(entry);
		}
	}

	public void addEntry(Entry entry) {
		addEntry(entry, entries.size());
	}

	public void addEntry(Entry entry, int index) {
		JSONArray rawEntries = data.getJSONArray(ENTRIES_KEY);

		touch();
		entries.add(index, entry);
		for (ListIterator<Entry> i = entries.listIterator(index); i.hasNext();) {
			rawEntries.put(index++, i.next().data);
		}
	}

	public Entry findFirst(String name) {
		Entry found = null;
		for (Entry entry : entries) {
			if (entry.getName().equals(name)) {
				found = entry;
			}
		}

		return found;
	}

	public FolderEntry findFirstFolder(String name) {
		FolderEntry found = null;
		for (Entry entry : entries) {
			if ((entry instanceof FolderEntry) && (entry.getName().equals(name))) {
				found = (FolderEntry) entry;
			}
		}

		return found;
	}

	public void removeFirstEntry(Entry entry) {
		int location = entries.indexOf(entry);

		if (location == -1) {
			System.err.println("WARNING: Entry not found during removal.");
			return;
		}

		JSONArray rawEntries = data.getJSONArray(ENTRIES_KEY);

		touch();
		entries.remove(entry);
		rawEntries.remove(location);
	}

	public void remove(int index) {
		JSONArray rawEntries = data.getJSONArray(ENTRIES_KEY);

		touch();
		entries.remove(index);
		rawEntries.remove(index);
	}

	public int indexOf(Entry entry) {
		return entries.indexOf(entry);
	}

	public Entry get(int index) {
		return entries.get(index);
	}

	public int size() {
		return entries.size();
	}

	@Override
	public String getType() {
		return FOLDER_TYPE;
	}

	@Override
	public Iterator<Entry> iterator() {
		return new EntryIterator<Entry>();
	}

	private class EntryIterator<T> implements Iterator<Entry> {
		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < entries.size();
		}

		@Override
		public Entry next() {
			if (this.hasNext()) {
				return entries.get(index++);
			}
			throw new NoSuchElementException();
		}

	}
}