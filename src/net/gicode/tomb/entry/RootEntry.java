/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb.entry;

import org.json.JSONObject;

public class RootEntry extends FolderEntry {
	public static final String ROOT_TYPE = "root";

	private static final String ROOT_NAME = "top-level-root";
	private static final String USER_AGENT_KEY = "user_agent";
	private static final String USER_AGENT = "net.gicode.tomb v1";

	public RootEntry() {
		super(ROOT_NAME, "");

		data.put(USER_AGENT_KEY, USER_AGENT);
	}

	public RootEntry(JSONObject data) {
		super(data);
	}

	public String export(int indentFactor) {
		return data.toString(indentFactor);
	}

	@Override
	public String getType() {
		return ROOT_TYPE;
	}
}
