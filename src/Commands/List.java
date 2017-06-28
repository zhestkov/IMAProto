package Commands;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import architecture.DB;

public class List extends Command {

	private int userId = 0;
	private PrintWriter out = null;
	private DB db = null;
	private String request = null;
	private String unmarkedResponse = "";
	private String markedResponse = null;

	private String reference; // 1st param: reference
	private String folder; // 2nd param: mailbox name

	public List(String prefix, String request, int userId, PrintWriter out, DB db) {
		this.prefix = prefix;
		this.request = request;
		this.userId = userId;
		this.out = out;
		this.db = db;
	}

	public void execute() throws SQLException {
		reference = getFirstParam(request);
		folder = getSecondParam(request);
		if (folder.equalsIgnoreCase("*")) {
			unmarkedResponse = makeUnmarkedAll();
			markedResponse = prefix + " OK LIST completed";
			sendResponse(unmarkedResponse, out);
		} else {
			boolean exists = isFolderExist();
			markedResponse = makeMarked(exists);
			if (exists) {
				unmarkedResponse = makeUnmarkedInner();
				sendResponse(unmarkedResponse, out);
			}
		}
		sendResponse(markedResponse, out);
	}

	public boolean isFolderExist() {
		boolean exists = false;
		try {
			exists = db.isFolderExist(folder, userId);
		} catch (SQLException e) {
			System.out.println("List: isFolderExist error");
			e.printStackTrace();
		}
		return exists;
	}

	public String makeUnmarkedAll() throws SQLException {
		String response = "";
		ResultSet rs = db.getAllFoldersByUser(userId);
		while (rs.next()) {
			response += "* LIST () \"/\" " + rs.getString("name") + "\r\n";
		}

		if (response.equalsIgnoreCase(""))
			response = "* LIST (\\NoInferiors) \"|\" ";
		return response;
	}

	public String makeUnmarkedInner() throws SQLException {
		String response = "";
		if (reference.equalsIgnoreCase("")) {
			ResultSet rs = db.getInnerFolders(folder, userId);
			if (rs != null) {
				while (rs.next()) {
					response += "* LIST () \"/\" " + rs.getString("name") + "\r\n";
				}
			} else
				response = "* LIST (\\NoInferiors) \"|\" " + folder;
		}

		return response;
	}

	public String makeMarked(boolean exists) {
		String response = "";
		if (exists)
			response = prefix + " OK LIST completed";
		else
			response = prefix + " BAD LIST can't find this mailbox";
		return response;
	}
}
