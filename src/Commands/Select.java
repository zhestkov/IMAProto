package Commands;

import java.io.PrintWriter;
import java.sql.SQLException;

import architecture.ClientHandler;
import architecture.DB;
import architecture.IncomingServer;

public class Select extends Command {
	private PrintWriter out = null;
	private String request = null;
	private String folder = null;
	private int userId = -1;
	private int cntExist = 0;
	private int cntUnseen = 0;
	private int cntRecent = 0;
	private int firstUnseen = 0;
	private int UIDVALIDITY = 0;
	private DB db;

	public Select(String prefix, String request, int userId, int UIDVALIDITY, DB db, PrintWriter out,
			ClientHandler handler) {
		this.prefix = prefix;
		this.request = request;
		this.userId = userId;
		this.out = out;
		this._handler = handler;
		this.db = db;
		this.folder = getFirstParam(this.request);
		this.UIDVALIDITY = UIDVALIDITY;
	}
	
	public Select(String prefix, String request, int userId, int UIDVALIDITY, DB db, PrintWriter out,
			IncomingServer incServer) {
		this.prefix = prefix;
		this.request = request;
		this.userId = userId;
		this.out = out;
		this._server = incServer;
		this.db = db;
		this.folder = getFirstParam(this.request);
		this.UIDVALIDITY = UIDVALIDITY;
	}

	public int getUIDVALIDITY() {
		return UIDVALIDITY;
	}

	public String getFolder() {
		return folder;
	}

	public void execute() {
		String unmarked = null;
		String marked = null;
		UIDVALIDITY++;
		try {
			cntExist = db.countExistMessages(userId, folder);
		} catch (SQLException e) {
			System.out.println("countExistMessages error.");
			e.printStackTrace();
		}
		try {
			cntRecent = db.countRecentMessages(userId, folder);
		} catch (SQLException e) {
			System.out.println("countExistMessages error.");
			e.printStackTrace();
		}
		try {
			firstUnseen = db.firstUnseen(userId, folder);
		} catch (SQLException e) {
			System.out.println("countExistMessages error.");
			e.printStackTrace();
		}
		try {
			cntUnseen = db.countUnseentMessages(userId, folder);
		} catch (SQLException e) {
			System.out.println("countUnseenMessages error.");
			e.printStackTrace();
		}

		unmarked = unmarkedResponse();
		marked = markedResponse();
		sendResponse(unmarked, out);
		sendResponse(marked, out);
	}

	private String unmarkedResponse() {
		String response = "";
		response = "* " + cntExist + " EXISTS\n" + "* " + cntRecent + " RECENT\n" + "* OK [UNSEEN " + cntUnseen + "] "
				+ firstUnseenSentence() + "\n" + "* OK [UIDVALIDITY " + UIDVALIDITY + "] UIDs valid\n"
				// TO DO: add UIDNEXT sample, e.g.: * OK [UIDNEXT 4392]
				// Predicted next UID
				+ "* FLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft)\n"
				+ "* OK [PERMANENTFLAGS (\\Deleted \\Seen \\*)] Limited";
		return response;
	}

	private String markedResponse() {
		// "A142 OK [READ-WRITE] SELECT completed"
		return prefix + " OK [READ-WRITE] SELECT completed";
	}

	private String firstUnseenSentence() {
		return "Message " + firstUnseen + " is first unseen";
	}

}
