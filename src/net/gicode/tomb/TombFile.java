/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.gicode.tomb.entry.Entry;
import net.gicode.tomb.entry.FolderEntry;
import net.gicode.tomb.entry.RootEntry;

public class TombFile {
	public static final String SOFTWARE_VERSION = Objects
			.toString(TombFile.class.getPackage().getImplementationVersion(), "dev");

	private static final String MAGIC = ".TOMB\377";
	private static final byte CONTAINER_VERSION = 2;
	private static final byte BCRYPT_COST = 13;
	private static final int SALT_SIZE = 128 / 8;
	private static final int IV_SIZE = 96 / 8;

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

		try (OutputStream out = new FileOutputStream(location)) {
			out.write(MAGIC.getBytes("UTF-8"));
			out.write(CONTAINER_VERSION);

			byte[] salt = new byte[SALT_SIZE];
			random.nextBytes(salt);
			out.write(salt);
			out.write(BCRYPT_COST);

			byte[] key = BCrypt.generate((password + "\000").getBytes("UTF-8"), salt, BCRYPT_COST);
			KeyParameter keySpec = new KeyParameter(key);

			byte[] iv = new byte[IV_SIZE];
			random.nextBytes(iv);
			out.write(iv);

			AEADBlockCipher cipher = new GCMBlockCipher(new AESEngine());
			cipher.init(true, new AEADParameters(keySpec, 128, iv));

			try (DeflaterOutputStream deflaterStream = new DeflaterOutputStream(new CipherOutputStream(out, cipher))) {
				deflaterStream.write(root.export(JSON_INDENT).getBytes("UTF-8"));
			}
		} catch (FileNotFoundException e) {
			throw new TombException("Unable to open file " + location + " for writing.");
		} catch (IOException e) {
			throw new TombException("Error writing file " + location + " (" + e.getMessage() + ").");
		}
	}

	public void load(String location, String password) throws TombException {

		try (InputStream in = new FileInputStream(location); DataInputStream din = new DataInputStream(in)) {

			byte[] desiredMagic = MAGIC.getBytes("UTF-8");
			byte[] magic = new byte[desiredMagic.length];
			din.readFully(magic);
			if (!Arrays.equals(desiredMagic, magic)) {
				throw new TombException("File " + location + " is not a valid Tomb file (invalid magic).");
			}

			byte version = din.readByte();
			if (version != CONTAINER_VERSION) {
				throw new TombException("File " + location + " version unsupported (" + version + ").");
			}

			byte[] salt = new byte[SALT_SIZE];
			din.readFully(salt);
			byte bcrypt_cost = din.readByte();

			byte[] key = BCrypt.generate((password + "\000").getBytes("UTF-8"), salt, bcrypt_cost);
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
}