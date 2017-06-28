package Commands;

import java.io.PrintWriter;
import java.sql.SQLException;

import architecture.DB;

public class Status extends Command {
	private PrintWriter out = null;
	private String request = null;
	private int userId = -1;
	private DB db;
	private String folder = null;
	private String unmarked = null;
	private String marked = null;

	public Status(String prefix, String request, int userId, PrintWriter out, DB db) {
		this.prefix = prefix;
		this.request = request;
		this.userId = userId;
		this.out = out;
		this.db = db;
		this.folder = getFirstParam(request);
	}

	public void execute() {
		marked = makeMarked();
		unmarked = makeUnmarked();
		sendResponse(unmarked, out);
		sendResponse(marked, out);
	}

	public String makeUnmarked() {
		String response = prefix + " OK STATUS completed";
		return response;
	}

	public String makeMarked() {
		String response = "* STATUS " + "\"" + folder + "\"" + " (";

		if (containsIgnoreCase(request, "UIDNEXT")) {
			int uidNext = 1;
			try {
				uidNext = db.getNextMessageUID(userId, folder);
			} catch (SQLException e) {
				System.out.println("Status: getNextMessageUID error");
				e.printStackTrace();
			}
			
			response += " UIDNEXT " + uidNext;
		}
		if (containsIgnoreCase(request, "MESSAGES")) {
			int cntExist = 0;
			try {
				cntExist = db.countExistMessages(userId, folder);
			} catch (SQLException e) {
				System.out.println("Status: countExistMessages error");
				e.printStackTrace();
			}
			response += " MESSAGES " + cntExist;
		}
		if (containsIgnoreCase(request, "UNSEEN")) {
			int cntUnseen = 0;
			try {
				cntUnseen = db.countUnseentMessages(userId, folder);
			} catch (SQLException e) {
				System.out.println("Status: countUnseenMessages error");
				e.printStackTrace();
			}
			response += " UNSEEN " + cntUnseen;
		}
		if (containsIgnoreCase(request, "RECENT")) {
			int cntRecent = 0;
			try {
				cntRecent = db.countRecentMessages(userId, folder);
			} catch (SQLException e) {
				System.out.println("Status: countRecentMessages error");
				e.printStackTrace();
			}
			response += " RECENT " +cntRecent;
		}
		response += ")";

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
