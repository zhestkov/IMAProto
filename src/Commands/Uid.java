package Commands;

import java.io.PrintWriter;

import architecture.DB;
import architecture.IncomingServer;

public class Uid extends Command {
	private PrintWriter out = null;
	private DB db = null;
	private String request = null;
	private int userId = -1;
	private String folder = null;
	private String unmarked = null;
	private String marked = null;

	public Uid(String prefix, String request, int userId, String folder, PrintWriter out, DB db,
			IncomingServer incServer) {
		this.prefix = prefix;
		this.request = request;
		this.userId = userId;
		this.folder = folder;
		this.out = out;
		this.db = db;
		this._server = incServer;
	}
	// e.g.:
	// C: A999 UID FETCH 4827313:4828442 FLAGS
	// S: * 23(number) FETCH (FLAGS (\Seen) UID 4827313)
	// S: * 24 FETCH (FLAGS (\Seen) UID 4827943)

	public void execute() {
		String response = "";
		if (containsIgnoreCase(request, "FETCH")) {
			String[] splited = request.split(" ");
//			for (String str : splited)
//				System.out.println("splited[i]: " + str);
			if (splited[2].equalsIgnoreCase("FETCH")) {
				String args = "";
				for (int i = 3; i < splited.length; i++)
					args += splited[i] + " ";
				String reqFetch = "FETCH " + args;
				reqFetch = reqFetch.toUpperCase();
				System.out.println("UID:reqFetch: " + reqFetch);
				Fetch fetch = new Fetch(prefix, reqFetch, folder, userId, out, db, _server);
				fetch.fetchExecute();
				//response = prefix + " OK UID FETCH completed";
				//sendResponse(response, out);
			}
		} else if (containsIgnoreCase(request, "SEARCH")) {
			String[] splited = request.split(" ");
			if (splited[2].equalsIgnoreCase("SEARCH")) {
				String args = "";
				for (int i = 3; i < splited.length; i++)
					args += splited[i] + " ";
				String reqSearch = "SEARCH " + args;
				reqSearch = reqSearch.toUpperCase();
				System.out.println("UID:reqSearch: " + reqSearch);
				Search search = new Search(prefix, request, userId, folder, out, db);
				search.execute();
			}
		} else if (containsIgnoreCase(request, "STORE")) {
			String[] splited = request.split(" ");
			if (splited[2].equalsIgnoreCase("STORE")) {
				String args = "";
				for (int i = 3; i < splited.length; i++)
					args += splited[i] + " ";
				String reqStore = "SEARCH " + args;
				System.out.println("UID:reqStore: " + reqStore);
				Store store = new Store(prefix, request, db, out);
				store.execute();
			}
		}
		else {
			response = prefix + " BAD UID unknown command";
			sendResponse(response, out);
		}
		
	}

	public String makeUnmarked() {
		String response = "";
		return response;
	}

	public String makeMarked() {
		String response = prefix + " BAD UID unknown command";
		return response;
	}

	public static boolean containsIgnoreCase(String str, String searchStr) {
		if (str == null || searchStr == null)
			return false;

		final int length = searchStr.length();
		if (length == 0)
			return true;

		for (int i = str.length() - length; i >= 0; i--) {
			if (str.regionMatches(true, i, searchStr, 0, length))
				return true;
		}
		return false;
	}

}
