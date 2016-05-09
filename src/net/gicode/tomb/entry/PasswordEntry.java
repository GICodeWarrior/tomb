/*
 * Copyright (c) 2016 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.entry;

import org.json.JSONObject;

public class PasswordEntry extends Entry {
	public static final String PASSWORD_TYPE = "password";

	private static final String USER_KEY = "user";
	private static final String PASSWORD_KEY = "password";

	public PasswordEntry(String name, String description, String username, String password) {
		super(name, description);

		data.put(USER_KEY, username);
		data.put(PASSWORD_KEY, password);
	}

	public PasswordEntry(JSONObject data) {
		super(data);
	}

	public String getUsername() {
		return data.getString(USER_KEY);
	}

	public void setUsername(String username) {
		touch();
		data.put(USER_KEY, username);
	}

	public String getPassword() {
		return data.getString(PASSWORD_KEY);
	}

	public void setPassword(String password) {
		touch();
		data.put(PASSWORD_KEY, password);
	}

	@Override
	public String getType() {
		return PASSWORD_TYPE;
	}

	@Override
	public String toString() {
		String[] entries = { super.toString(), "User: " + getUsername(), "Pass: " + getPassword() };

		return String.join("\n", entries);
	}
}
