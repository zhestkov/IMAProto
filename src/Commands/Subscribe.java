package Commands;

import java.io.PrintWriter;

public class Subscribe extends Command {
	public PrintWriter out;
	public String unmarked;
	public String marked;

	public Subscribe(String prefix, PrintWriter out) {
		this.prefix = prefix;
		this.out = out;
	}

	public void execute() {
		marked = makeMarked();
		sendResponse(marked, out);
	}

	public String makeMarked() {
		String response = prefix + " BAD SUBSCRIBE illegal command";
		return response;
	}

	// TO DO:
	public String makeUnmarked() {
		String response = "";
		return response;
	}
}
