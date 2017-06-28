package Commands;

import architecture.ClientHandler;
import architecture.IncomingServer;

import java.io.PrintWriter;

public class Command {
	protected String prefix = null;
	protected ClientHandler _handler = null;
	
	protected IncomingServer _server = null;

//	public Command(ClientHandler handler) {
//		this._handler = handler;
//	}
	
	public void sendResponse(String response, PrintWriter out) {
		out.println(response);
		System.out.println("The response was sent to client: " + response);
	}

	// get params from client request
	public String getFirstParam(String request) {
		return request.substring(request.indexOf("\"") + 1, request.indexOf("\"", request.indexOf("\"") + 1));
	}

	public String getSecondParam(String request) {
		int indexSecond = request.indexOf("\"", request.indexOf("\"") + 1);
		int startSecond = request.indexOf("\"", indexSecond+1);
		int endSecond = request.indexOf("\"", startSecond+1);
		return request.substring(startSecond+1, endSecond);
		//return request.substring(indexSecond + 3, request.length() - 1);
	}
	protected String invalidCmd() {
		return "* BAD - command unknown or arguments invalid";
	}
}
