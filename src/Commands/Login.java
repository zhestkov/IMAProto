package Commands;

import java.io.PrintWriter;
import java.sql.SQLException;

import architecture.ClientHandler;
import architecture.IncomingServer;
import architecture.User;

public class Login extends Command {
	private String request = null;
	private String response = null;
	private PrintWriter out = null;
	private String login = null;
	private String password = null;
	private int userId = -1;
	private IncomingServer _server = null;
	private User user = null;
	
	public Login(String prefix, String request, PrintWriter out, ClientHandler handler) {
		this.prefix = prefix;
		this.request = request;
		this.out = out;
		this._handler = handler;
	}
	public Login(String prefix, String request, PrintWriter out, User user, IncomingServer server) {
		this.prefix = prefix;
		this.request = request;
		this.out = out;
		this._server = server;		
		this.user = user;
	}
	public int getUserId() {
		return this.userId;
	}
	public boolean findUser() throws SQLException {
		login = getFirstParam(request);
		password = getSecondParam(request);
		return _handler.findUser(login, password);
	}
	
	public boolean findUser1() throws SQLException {
		login = getFirstParam(request);
		password = getSecondParam(request);
		//return _handler.findUser(login, password);
		return _server.findUser(login, password);
	}
	
	
	public String makeResponse(boolean userExists) {
		if (userExists) {
			for (User user : _handler._server.users)
				if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
					user.setState(1); // 1 - auth-ed // logged in
					this.userId = user.getId(); // update userId values in ClientHandler for following commands (e.g. SELECT)
					break;
				}
			return prefix + " OK LOGIN completed";
		}
		else return prefix + " NO LOGIN failed";
	}
	// single
	public String makeResponse1(boolean userExists) {
		if (userExists) {
			for (User user : _server.users)
				if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
					user.setState(1); // 1 - auth-ed // logged in
					this.userId = user.getId(); // update userId values in ClientHandler for following commands (e.g. SELECT)
					break;
				}
			return prefix + " OK LOGIN completed";
		}
		else return prefix + " NO LOGIN failed";
	}
	public void execute() throws SQLException {
		//boolean exists = findUser();
		boolean exists = findUser1(); // single
		//response = makeResponse(exists);
		response = makeResponse1(exists);
		sendResponse(response, out);
		// single:
		user.setId(userId);
		user.setLogin(login);
		user.setPassword(password);
		user.setState(1); // logged in
		
		// multi:
//		_handler.user.setId(userId);
//		_handler.user.setLogin(login);
//		_handler.user.setPassword(password);
//		_handler.user.setState(1); // 1 = logged in
	}
	
	

}
