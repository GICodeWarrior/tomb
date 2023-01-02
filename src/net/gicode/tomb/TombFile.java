/*
 * Copyright (c) 2023 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.gicode.tomb.entry.Entry;
import net.gicode.tomb.entry.FolderEntry;
import net.gicode.tomb.entry.RootEntry;

public class TombFile {
	public static final String SOFTWARE_VERSION = Objects
			.toString(TombFile.class.getPackage().getImplementationVersion(), "dev");

	private static final byte[] MAGIC = { '.', 'T', 'O', 'M', 'B', (byte) 0xFF, (byte) 0x00 };
	private static final byte[] PREVIOUS_MAGIC = { '.', 'T', 'O', 'M', 'B', (byte) 0xC3, (byte) 0xBF };
	private static final byte CONTAINER_VERSION_BCRYPT = 2; // Deprecated
	private static final byte CONTAINER_VERSION_ARGON2ID = 3;

	private static final int SALT_SIZE = 128 / 8;
	private static final int ARGON2_MEMORY = 1 << 20; // 1GB
	private static final int ARGON2_ITERATIONS = 1;
	private static final int ARGON2_PARALLELISM = 1;

	private static final int IV_SIZE = 96 / 8;
	private static final int AES_KEY_SIZE = 128 / 8;

	private static final int JSON_INDENT = 2;

	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	private RootEntry root;

	public TombFile() {
		root = new RootEntry();
	}

	public TombFile(RootEntry root) {
		this.root = root;
	}

	public RootEntry getRoot() {
		return root;
	}

	public void save(String location, String password) throws TombException {
		SecureRandom random;

		try {
			random = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			// XXX: PANIC
			e.printStackTrace();
			System.exit(1);
			return;
		}

		try (OutputStream out = new FileOutputStream(location); DataOutputStream dout = new DataOutputStream(out)) {
			dout.write(MAGIC);
			dout.write(CONTAINER_VERSION_ARGON2ID);

			byte[] salt = new byte[SALT_SIZE];
			random.nextBytes(salt);
			dout.write(salt);
			dout.writeInt(ARGON2_MEMORY);
			dout.writeInt(ARGON2_ITERATIONS);
			dout.writeInt(ARGON2_PARALLELISM);

			byte[] key = computeArgon2(password, salt, ARGON2_MEMORY, ARGON2_ITERATIONS, ARGON2_PARALLELISM);
			KeyParameter keySpec = new KeyParameter(key);

			byte[] iv = new byte[IV_SIZE];
			random.nextBytes(iv);
			dout.write(iv);

			AEADBlockCipher cipher = new GCMBlockCipher(new AESEngine());
			cipher.init(true, new AEADParameters(keySpec, 128, iv));

			try (DeflaterOutputStream deflaterStream = new DeflaterOutputStream(new CipherOutputStream(dout, cipher))) {
				deflaterStream.write(root.export(JSON_INDENT).getBytes("UTF-8"));
			}
		} catch (FileNotFoundException e) {
			throw new TombException("Unable to open file " + location + " for writing.");
		} catch (IOException e) {
			throw new TombException("Error writing file " + location + " (" + e.getMessage() + ").");
		} finally {
			// Encourage the JVM to release our Argon2 memory.
			System.gc();
		}
	}

	public void load(String location, String password) throws TombException {

		try (InputStream in = new FileInputStream(location); DataInputStream din = new DataInputStream(in)) {

			byte[] magic = new byte[MAGIC.length];
			din.readFully(magic);
			if (!Arrays.equals(MAGIC, magic) && !Arrays.equals(PREVIOUS_MAGIC, magic)) {
				throw new TombException("File " + location + " is not a valid Tomb file (invalid magic).");
			}

			byte version = din.readByte();
			byte[] key = null;

			if (version == CONTAINER_VERSION_BCRYPT) {
				// Deprecated
				byte[] salt = new byte[SALT_SIZE];
				din.readFully(salt);
				byte bcrypt_cost = din.readByte();

				// Append null to prevent repetitive key truncation. BCrypt tiles the password
				// out to a constant width, so a repetitive password with a length that is a
				// factor of that constant width will be equivalent to a shorter version of the
				// same (e.g. testtest is equivalent to test).
				key = BCrypt.generate((password + "\000").getBytes("UTF-8"), salt, bcrypt_cost);

			} else if (version == CONTAINER_VERSION_ARGON2ID) {
				byte[] salt = new byte[SALT_SIZE];
				din.readFully(salt);

				int memory = din.readInt();
				int iterations = din.readInt();
				int parallelism = din.readInt();

				key = computeArgon2(password, salt, memory, iterations, parallelism);

			} else {
				throw new TombException("File " + location + " version unsupported (" + version + ").");
			}

			KeyParameter keySpec = new KeyParameter(key);

			byte[] iv = new byte[IV_SIZE];
			din.readFully(iv);

			AEADBlockCipher cipher = new GCMBlockCipher(new AESEngine());
			cipher.init(false, new AEADParameters(keySpec, 128, iv));

			byte[] compressedData = null;
			try (CipherInputStream cipherStream = new CipherInputStream(din, cipher)) {
				// Data must be fully decrypted before GCM auth check occurs D:
				compressedData = IOUtils.toByteArray(cipherStream);
			}

			try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
					InflaterInputStream inflaterStream = new InflaterInputStream(bis)) {

				root = new RootEntry(new JSONObject(new JSONTokener(inflaterStream)));
			}
		} catch (FileNotFoundException e) {
			throw new TombException("File " + location + " not found.");
		} catch (IOException e) {
			throw new TombException("Error reading file " + location + " (" + e.getMessage() + ").");
		} finally {
			// Encourage the JVM to release our Argon2 memory.
			System.gc();
		}
	}

	public Instant getUpdated() {
		return getUpdated(root);
	}

	private Instant getUpdated(Entry current) {
		if (current instanceof FolderEntry) {
			Instant newest = current.getUpdated();
			for (Entry entry : (FolderEntry) current) {
				Instant updated = getUpdated(entry);
				if (updated.isAfter(newest)) {
					newest = updated;
				}
			}
			return newest;
		}
		return current.getUpdated();
	}

	private byte[] computeArgon2(String password, byte[] salt, int memory, int iterations, int parallelism) {
		Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
				.withSalt(salt)
				.withMemoryAsKB(memory)
				.withIterations(iterations)
				.withParallelism(parallelism)
				.build();

		Argon2BytesGenerator generator = new Argon2BytesGenerator();
		generator.init(params);

		byte[] key = new byte[AES_KEY_SIZE];
		generator.generateBytes(password.toCharArray(), key);

		return key;
	}
}