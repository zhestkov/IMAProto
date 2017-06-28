package Commands;

import java.io.PrintWriter;

public class Lsub extends Command {
	private PrintWriter out;
	//private String unmarked;
	private String marked;
	
	public Lsub(String prefix, PrintWriter out) {
		this.prefix = prefix;
		this.out = out;
	}
	public void execute() {
		//marked = makeMarked();
		marked = "* LSUB (\\Marked \\HasChildren)" +  " \".\" " + "\"INBOX\"\n"
				+ "* LSUB (\\HasNoChildren)" +  " \".\" " + "\"INBOX.InnerFolder\"\n";
		sendResponse(marked, out);
		marked = prefix + " OK LSUB completed";
		sendResponse(marked, out);
	}
	// unsupported command
	public String makeMarked() {
		String response = prefix + " BAD LSUB unknown command";
		return response;
	}
}
