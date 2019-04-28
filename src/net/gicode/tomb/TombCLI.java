/*
 * Copyright (c) 2019 Rusty Burchfield
 *
 * This software may be modified and distributed under the terms
 * of the MIT License.  See the LICENSE file for details.
 */
package net.gicode.tomb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.gicode.tomb.entry.Entry;
import net.gicode.tomb.entry.FolderEntry;
import net.gicode.tomb.entry.PasswordEntry;
import net.gicode.tomb.entry.RootEntry;

public class TombCLI {
	private TombFile file = new TombFile();

	public void init(String location, String password) {
		save(location, password);
	}

	public void exportJSON(String location, String password) {
		load(location, password);
		System.out.println(file.getRoot().export(2));
	}

	private void importJSON(String location) {
		String password = JOptionPane.showInputDialog("Password");
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		try {
			while ((length = System.in.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		RootEntry root = null;
		try {
			root = new RootEntry(new JSONObject(new JSONTokener(result.toString("UTF-8"))));
		} catch (JSONException | UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
		}
		file = new TombFile(root);
		save(location, password);
	}

	public void addPass(String location, String filePassword, String[] folders, String name, String description,
			String username, String password) {
		load(location, filePassword);

		FolderEntry folder = file.getRoot();
		for (String folderName : folders) {
			folder = folder.findFirstFolder(folderName);
		}

		PasswordEntry entry = new PasswordEntry(name, description, username, password);
		folder.addEntry(entry);

		save(location, filePassword);
		System.out.println("Password added.");
	}

	public void addFolder(String location, String filePassword, String[] folders, String name, String description) {
		load(location, filePassword);

		FolderEntry folder = file.getRoot();
		for (String folderName : folders) {
			folder = folder.findFirstFolder(folderName);
		}

		FolderEntry entry = new FolderEntry(name, description);
		folder.addEntry(entry);

		save(location, filePassword);
		System.out.println("Folder added.");
	}

	public void remove(String location, String password, String[] folders, String name) {
		load(location, password);

		FolderEntry folder = file.getRoot();
		for (String folderName : folders) {
			folder = folder.findFirstFolder(folderName);
		}

		Entry entry = folder.findFirst(name);
		folder.removeFirstEntry(entry);

		save(location, password);
		System.out.println("Removed entry.");
	}

	public void search(String location, String password, String regex) {
		load(location, password);

		FolderEntry folder = file.getRoot();
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		searchFolder(folder, new LinkedList<String>(), pattern);
	}

	private void searchFolder(FolderEntry folder, List<String> parents, Pattern pattern) {
		for (Entry entry : folder) {
			if (entry instanceof FolderEntry) {
				parents.add(entry.getName());
				searchFolder((FolderEntry) entry, parents, pattern);
				parents.remove(parents.size() - 1);
			}

			Matcher matcher = pattern.matcher(entry.getName());
			if (matcher.find()) {
				System.out.println();
				if (!parents.isEmpty()) {
					System.out.println("Folder: " + String.join(" > ", parents));
				}
				System.out.println(entry);
			}
		}
	}

	public void listFolder(String location, String password, String[] folders) {
		load(location, password);

		FolderEntry folder = file.getRoot();
		List<String> parents = new LinkedList<String>();
		for (String folderName : folders) {
			folder = folder.findFirstFolder(folderName);
			parents.add(folderName);
		}

		System.out.println("Folder: " + String.join(" > ", parents));
		for (Entry entry : folder) {
			System.out.println();
			System.out.println(entry);
		}
	}

	private void load(String location, String password) {
		try {
			file.load(location, password);
		} catch (TombException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private void save(String location, String password) {
		try {
			file.save(location, password);
		} catch (TombException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private void generate(int length) {
		for (int c = 0; c < 10; ++c) {
			System.out.println(RandomPassword.generate(length));
		}
	}

	public static void main(String[] args) {
		TombCLI cli = new TombCLI();

		String command = "";
		if (args.length > 0) {
			command = args[0];
		}

		String location;
		String[] folders;

		switch (command) {
		case "init":
			verifyArgsLength(args, 2);
			cli.init(args[1], readPassword());
			break;
		case "export":
			verifyArgsLength(args, 2);
			cli.exportJSON(args[1], readPassword());
			break;
		case "import":
			verifyArgsLength(args, 2);
			cli.importJSON(args[1]);
			break;
		case "addPass":
			verifyMinArgs(args, 6);

			location = args[1];
			folders = new String[args.length - 6];
			System.arraycopy(args, 2, folders, 0, folders.length);

			cli.addPass(location, readPassword(), folders, args[args.length - 4], args[args.length - 3],
					args[args.length - 2], args[args.length - 1]);
			break;
		case "addFolder":
			verifyMinArgs(args, 4);

			location = args[1];
			folders = new String[args.length - 4];
			System.arraycopy(args, 2, folders, 0, folders.length);

			cli.addFolder(location, readPassword(), folders, args[args.length - 2], args[args.length - 1]);
			break;
		case "remove":
			verifyMinArgs(args, 3);

			location = args[1];
			folders = new String[args.length - 3];
			System.arraycopy(args, 2, folders, 0, folders.length);

			cli.remove(location, readPassword(), folders, args[args.length - 1]);
			break;
		case "search":
			verifyArgsLength(args, 3);

			cli.search(args[1], readPassword(), args[2]);
			break;
		case "listFolder":
			verifyArgsLength(args, 3);

			folders = new String[args.length - 2];
			System.arraycopy(args, 2, folders, 0, folders.length);

			cli.listFolder(args[1], readPassword(), folders);
			break;
		case "generate":
			if (args.length > 2) {
				System.err.println("Wrong number of arguments (" + args.length + ").  Need 0 or 1.");
				System.exit(1);
			}

			int length = 16;
			if (args.length == 2) {
				length = Integer.parseInt(args[1]);
			}

			cli.generate(length);
			break;
		default:
			if (args.length > 0) {
				System.err.println("Unknown command: " + command);
			} else {
				System.err.println("No command specified.");
			}
		case "help":
			System.err.println("Tomb v" + TombFile.SOFTWARE_VERSION);
			System.err.println("Commands:");
			System.err.println("  init FILE");
			System.err.println("  export FILE");
			System.err.println("  import FILE");
			System.err.println("  addPass FILE FOLDER... NAME DESCRIPTION USERNAME PASSWORD");
			System.err.println("  addFolder FILE FOLDER... NAME DESCRIPTION");
			System.err.println("  remove FILE [FOLDER...] NAME");
			System.err.println("  search FILE KEYWORD");
			System.err.println("  listFolder FILE FOLDER...");
			System.err.println("  generate [LENGTH]");
		}
	}

	private static String readPassword() {
		return new String(System.console().readPassword("Password: "));
	}

	private static void verifyArgsLength(String[] args, int length) {
		if (args.length != length) {
			System.err.println("Wrong number of arguments (" + args.length + ").  Need " + length + ".");
		}
	}

	private static void verifyMinArgs(String[] args, int min) {
		if (args.length < min) {
			System.err.println("Too few arguments (" + args.length + ").  Minimum of " + min + ".");
			System.exit(1);
		}
	}
}
