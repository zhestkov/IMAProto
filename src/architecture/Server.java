package architecture;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class Server extends Thread {
	private int port = 143; // default: 143; SSL: 993
	private int currUserId = 0;
	public ArrayList<ClientHandler> clientHandlers = null;
	public ArrayList<Mailbox> folders = null;
	public ServerSocket sockServer = null;
	public Socket sockClient = null;
	public ArrayList<User> users = null;
	public ArrayList<Socket> sockets = null;
	public DB db;
	public int userId;
	public int UIDVALIDITY = 0;
	//public String folder;
	public static final String[] CAPABILITY2 = new String[] { "capability", "CAPABILITY" };
	// public static final String[] LOGIN = new String[]{"login", "LOGIN"};
	// public static final String[] SELECT = new String[]{"select", "SELECT"};
	// public static final String[] SEARCH = new String[]{"search", "SEARCH"};
	// public static final String[] FETCH = new String[]{"fetch", "FETCH"};
	// public static final String[] NOOP = new String[]{"noop", "NOOP"};
	// public static final String[] LOGOUT = new String[]{"logout", "LOGOUT"};
	// public static final String[] LSUB = new String[]{"lsub", "LSUB"};
	// public static final String[] LIST = new String[]{"list", "LIST"};
	// public static final String[] CREATE = new String[]{"create", "CREATE"};
	// public static final String[] SUBSCRIBE = new String[]{"subscribe",
	// "SUBSCRIBE"};
	// public static final String[] COPY = new String[]{"copy", "COPY"};
	// public static final String[] STATUS = new String[]{"status", "STATUS"};
	// public static final String[] STORE = new String[]{"store", "STORE"};
	public static final String CAPABILITY = "capability";
	public static final String LOGIN = "login";
	public static final String SELECT = "select";
	public static final String SEARCH = "search";
	public static final String FETCH = "fetch";
	public static final String NOOP = "noop";
	public static final String LOGOUT = "logout";
	public static final String LSUB = "lsub";
	public static final String LIST = "list";
	public static final String CREATE = "create";
	public static final String SUBSCRIBE = "subscribe";
	public static final String COPY = "copy";
	public static final String STATUS = "status";
	public static final String STORE = "store";
	public static final String[] FLAGS = new String[] { "Answered", "Recent", "Deleted", "Seen", "Draft", "Flagged" };

	public Server(int port) {
		this.port = port;
		currUserId = 0;
		sockets = new ArrayList<Socket>();
		db = new DB(this);
		users = getUsers();
		folders = getFolders();
		printUsers();
	}
	public void printUsers() {
		System.out.println("USERS LIST:");
		for (User user : users)
			System.out.println(user); 
	}

	public void run() {
		try {
			sockServer = new ServerSocket(port);
		} catch (IOException ioe) {
			System.out.println("cant' open Server Socket.");
			System.exit(-1);
		}
		System.out.println("Server is running. Port: " + port);
		// waiting for the client
		while (true) {
			sockClient = null;
			try {
				System.out.println("Waiting for a client...");
				sockClient = sockServer.accept();
			} catch (IOException ioe) {
				System.out.println("Can't accept the listen socket.");
				System.exit(-1);
			}
			sockets.add(sockClient);
			ClientHandler clientHandler = new ClientHandler(sockets.get(currUserId++), this);
			System.out.println("1: " + sockClient);
			// clientHandlers.add(clientHandler);
			System.out.println("Starting clientHandler...");
			clientHandler.start();
		}
	}
	public ArrayList<User> getUsers() {
		try {
			return db.getUsers();
		} catch (SQLException e) {
			System.out.println("Cant't get Users from database.");
			e.printStackTrace();
		}
		return null;
	}
	public ArrayList<Mailbox> getFolders() {
		try {
			return db.getFolders();
		} catch (SQLException e) {
			System.out.println("Can't get folders from database.");
			e.printStackTrace();
		}
		return null;
	}
	public User getUserById(int id) {
		for (User user : users)
			if (user.getId() == id)
				return user;
		return null;
	}
}
