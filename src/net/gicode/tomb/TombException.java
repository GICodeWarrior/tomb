/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb;

@SuppressWarnings("serial")
public class TombException extends Exception {
	public TombException(String message) {
		super(message);
	}
}
