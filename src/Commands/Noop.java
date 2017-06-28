package Commands;

import java.io.PrintWriter;
import java.sql.SQLException;

import architecture.DB;

public class Noop extends Command {
	private PrintWriter out;
	private String response = "";
	private int newMsgFlag = 0;
	private String folder = null;
	private int userId = -1;
	private DB db = null;
	
	public Noop(String prefix, String folder, int userId, PrintWriter out, DB db) {
		this.prefix = prefix;
		this.folder = folder;
		this.userId = userId;
		this.out = out;
		this.db = db;
	}
	
	private void makeResponse() {
		response = prefix + " OK NOOP completed";
	}
	private int getMsgFlag() throws SQLException {
		int flag = db.getNewMsgFlag(userId);
		if (flag > 0)
			System.out.println("Attention: new messages were added");
		return flag;
	}
	private int resetMsgFlag() throws SQLException {
		int res = db.resetNewMsgFlag(userId);
		return res;
	}
	public void execute() {
		int flag = 0;
		try {
			flag = getMsgFlag();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (flag == 0) {
			makeResponse();
			sendResponse(response, out);
		}
		else {
			makeResponse();
			String info = selectFolderResponse();
			sendResponse(info, out);
			sendResponse(response, out);
			try {
				resetMsgFlag();
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Couldn't reset newMsg flag after NOOP");
			}
		}
		
	}
	public String selectFolderResponse() {
		int cntExist = 0;
		int cntRecent = 0;
		int firstUnseen = 0;
		int cntUnseen = 0;
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
		String response = "";
		response = "* " + cntExist + " EXISTS\n" + "* " + cntRecent + " RECENT\n";
		return response;
	}
	
}
