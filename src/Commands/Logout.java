package Commands;

import java.io.PrintWriter;

public class Logout extends Command {
	private PrintWriter out = null;
	private String unmarked = null;
	private String marked = null;
	
	public Logout(String prefix, PrintWriter out) {
		this.prefix = prefix;
		this.out = out;
	}
	
	public void execute() {
		marked = makeMarked();
		unmarked = makeUnmarked();
		sendResponse(unmarked, out);
		sendResponse(marked, out);
	}
	
	public String makeUnmarked() {
		String response = "* BYE IMAP4rev1 server terminating connection";
		return response;
	}
	
	public String makeMarked() {
		String response = prefix + " LOGOUT completed";
		return response;
	}
}
