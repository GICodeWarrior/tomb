/*
 * Copyright (c) 2016 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.entry;

import java.time.Instant;

import org.json.JSONObject;

public abstract class Entry {
	protected static final String TYPE_KEY = "type";

	private static final String NAME_KEY = "name";
	private static final String DESCRIPTION_KEY = "description";

	private static final String CREATED_KEY = "created_at";
	private static final String UPDATED_KEY = "updated_at";

	protected JSONObject data;

	protected Entry(String name, String description) {
		data = new JSONObject();

		data.put(TYPE_KEY, getType());
		data.put(NAME_KEY, name);
		data.put(DESCRIPTION_KEY, description);

		long time = Instant.now().toEpochMilli();
		data.put(CREATED_KEY, time);
		data.put(UPDATED_KEY, time);
	}

	protected Entry(JSONObject data) {
		this.data = data;
	}

	abstract public String getType();

	public String getName() {
		return data.getString(NAME_KEY);
	}

	public void setName(String name) {
		touch();
		data.put(NAME_KEY, name);
	}

	public String getDescription() {
		return data.getString(DESCRIPTION_KEY);
	}

	public void setDescription(String description) {
		touch();
		data.put(DESCRIPTION_KEY, description);
	}

	public Instant getCreated() {
		return Instant.ofEpochMilli(data.getLong(CREATED_KEY));
	}

	public Instant getUpdated() {
		return Instant.ofEpochMilli(data.getLong(UPDATED_KEY));
	}

	@Override
	public String toString() {
		String[] entries = { "Name: " + getName(), "Description: " + getDescription(), "Type: " + getType(),
				"Created: " + getCreated(), "Updated: " + getUpdated() };
		return String.join("\n", entries);
	}

	protected void touch() {
		long time = Instant.now().toEpochMilli();
		data.put(UPDATED_KEY, time);
	}
}
