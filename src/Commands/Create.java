package Commands;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import architecture.DB;
import architecture.Mailbox;

public class Create extends Command {
	private PrintWriter out = null;
	private DB db = null;
	private int userId = -1;
	private String request = null;
	private String folder = null;
	private int parentId = 0; // 0 means tier-1 level folder

	public Create(String prefix, String request, int userId, PrintWriter out, DB db) {
		this.prefix = prefix;
		this.userId = userId;
		this.request = request;
		this.out = out;
		this.db = db;
		this.folder = getFirstParam(this.request);
	}
	

	public void execute() throws SQLException {
		String marked = markedResponse();
		sendResponse(marked, out);

	}

	public String markedResponse() {
		boolean exists = false;
		// ArrayList<String> foldersToCreate = new ArrayList<String>();
		try {
			exists = isFolderExist(folder, userId);
		} catch (SQLException e) {
			System.out.println("isFoldeExist error");
			e.printStackTrace();
		}
		if (exists)
			return prefix + " NO CREATE folder already exists";
		// foldersToCreate.add(folder);
		try {
			String currFolder = "";
			String parentFolder = null;
			String[] subfolders = folder.split("/");
			if (subfolders.length > 1) {
				for (int i = 0; i < subfolders.length - 1; i++) {
					// folder name sample:
					// way/from/left/to/right/to/create/inner/folders
					if (i == 0) {
						parentFolder = subfolders[i];
						currFolder += subfolders[i];
						if (!isFolderExist(currFolder, userId)) {
							if (!createFolder(currFolder, userId, 0))
								System.out.println("Can't create folder, level: " + i);
						}
					} else {
						currFolder += "/" + subfolders[i];

						if (!isFolderExist(currFolder, userId)) {
							try {
								int parentFolderId = db.getFolderId(parentFolder, userId);
								if (!createFolder(currFolder, userId, parentFolderId))
									System.out.println("Can't create folder, level: " + i);
							} catch (SQLException e) {
								System.out.println("Can't find folderId for this parentFolder name.");
								e.printStackTrace();
							}
						}
						parentFolder = currFolder;
					}
				}
			}

			boolean res = createFolder(folder, userId, parentId);
			if (res)
				return prefix + " OK CREATE completed";
		} catch (SQLException e) {
			System.out.println("createFolder error");
			e.printStackTrace();
		}
		return prefix + " NO CREATE failure: can't create mailbox with that name";
	}

	public boolean isFolderExist(String folder, int userId) throws SQLException {
		return db.isFolderExist(folder, userId);
	}

	public boolean createFolder(String folder, int userId, int parentId) throws SQLException {
		int res = db.createFolder(folder, userId, parentId);
		if (res != 0) {
			int folderId = db.getFolderId(folder, userId);
			if (folderId != -1) {
				//_server.folders.add(new Mailbox(folderId, folder, userId, parentId));
				System.out.println("New folder [" + folder + "] was successfully added.");
			}
			else
				System.out.println("Can't get folderId.");
			return true;
		}
		return false;
	}

}
