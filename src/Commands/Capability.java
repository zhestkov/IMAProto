package Commands;

import java.io.PrintWriter;

import architecture.ClientHandler;
import architecture.IncomingServer;

public class Capability extends Command {
	PrintWriter out = null;
	String unmarked = null;
	String marked = null;
	
	public Capability(String prefix, PrintWriter out, ClientHandler handler) {
		this._handler = handler;
		this.prefix = prefix;
		this.out = out;
	}
	
	public Capability (String prefix, PrintWriter out) {
		this.prefix = prefix;
		this.out = out;
	}
	public Capability(String prefix, PrintWriter out, IncomingServer server) {
		this.prefix = prefix;
		this.out = out;
		this._server = server;
	}
	public void execute() {
		unmarked = getResponse();
		marked = getPrefixableResponse();
		sendResponse(unmarked, out);
		sendResponse(marked, out);
	}
	public String getResponse() {
		return "* CAPABILITY IMAP4rev1";
	}
	public String getPrefixableResponse() {
		return prefix + " OK CAPABILITY completed";
	}
}
