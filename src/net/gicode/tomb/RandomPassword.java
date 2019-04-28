/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomPassword {
	public static final String NUMBERS = "0123456789";
	public static final String UPPERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String LOWERS = "abcdefghijklmnopqrstuvwxyz";
	public static final String ALPHAS = UPPERS + LOWERS;
	public static final String ALPHA_NUMERIC = NUMBERS + ALPHAS;

	public static String generate(int length) {
		return generate(length, ALPHA_NUMERIC);
	}

	public static String generate(int length, String charset) {
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			// XXX PANIC
			e.printStackTrace();
			System.exit(1);
		}

		StringBuilder password = new StringBuilder(length);
		int charsetLength = charset.length();
		for (int c = 0; c < length; ++c) {
			password.append(charset.charAt(random.nextInt(charsetLength)));
		}

		return password.toString();
	}
}
