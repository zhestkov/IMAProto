package Commands;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import architecture.DB;

public class Search extends Command {
	private PrintWriter out = null;
	private DB db =  null;
	private int userId = 0;
	private String folder = null;
	private String request = null;
	private String unmarked = null;
	private String marked = null;
	
	
	public Search(String prefix, String request, int userId, String folder, PrintWriter out, DB db) {
		this.prefix = prefix;
		this.request = request;
		this.userId = userId;
		this.folder = folder;
		this.out = out;
		this.db = db;
	}
	
	public void execute() {
		System.out.println("Search: folder = " + folder);
		unmarked = makeUnmarked();
		marked = makeMarked();
		sendResponse(unmarked, out);
		sendResponse(marked, out);
	}
	
	public String makeUnmarked() {
		String response = "* SEARCH";
		ResultSet rs = null;
		if (request.indexOf("DELETED") != -1) {
			rs = db.countFlaggedMessage(userId, folder, "Deleted", 1);
		}
		if (request.indexOf("SEEN") != -1) {
			rs = db.countFlaggedMessage(userId, folder, "Seen", 1);
		}
		if (request.indexOf("UNSEEN") != -1) {
			rs = db.countFlaggedMessage(userId, folder, "Seen", 0);
		}
		try {
			while (rs.next()) {
				response += " " + rs.getInt("id");
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public String makeMarked() {
		String response = prefix + " OK SEARCH completed";
		return response;
	}
	
}

