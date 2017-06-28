package architecture;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import Commands.*;

public class IncomingServer extends Thread {
	public int port = 0;
	private int currUserId = 0;
	public ArrayList<User> users = null;
	public BufferedReader in = null;
	public PrintWriter out = null;
	public ServerSocket sockServer = null;
	public Socket sockClient = null;
	public int UserId;
	public int UIDVALIDITY = 0;
	public String folder;
	public DB db = null;
	public ArrayList<Mailbox> folders = null;
	public User user = null;
	public static final String[] FLAGS = new String[] { "Answered", "Recent", "Deleted", "Seen", "Draft", "Flagged" };

	/**
	 * Конструктор IncomingServer
	 * 
	 * @param host_port
	 *            - порт, на котором развернут сервер
	 */
	public IncomingServer(int host_port) {
		this.port = host_port;
		folders = new ArrayList<Mailbox>();
		db = new DB(this);
		users = getUsers();
		folders = getFolders();
		printUsers();
		printFolders();
		// users.add(new User(currUserId, "abc", "test"));
	}

	/**
	 * Конструктор IncomingServer по-умолчанию
	 */
	public IncomingServer() {
	}

	public void printUsers() {
		System.out.println("USERS LIST:");
		for (User user : users)
			System.out.println(user);
	}

	public void printFolders() {
		System.out.println("FOLDERS LIST:");
		for (Mailbox folder : folders)
			System.out.println(folder.getName());
	}

	/**
	 * Запуск сервера Создает соединение, запускает процесс обмена сообщениями
	 * между клиентом и сервером
	 */
	public void run() {

		System.out.println("Server at " + port + " running");
		// creating server socket
		try {
			sockServer = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Couldn't listen to port " + port);
			System.exit(-1);
		}
		// connecting client
		try {
			System.out.println("Waiting for a client...");
			sockClient = sockServer.accept();
			System.out.println("Client connected");
		} catch (IOException e) {
			System.err.println("Can't accept");
			System.exit(-1);
		}
		// Buffer for echo
		try {
			in = new BufferedReader(new InputStreamReader(sockClient.getInputStream()));
			out = new PrintWriter(sockClient.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println("Couldn't make buffer");
			System.exit(-1);
		}

		// messaging
		out.println("* OK, IMAP4 service is ready");

		while (true) {

			messaging();
		}
	}

	/**
	 * Прием сообщения от клиента и передача его методу анализа сообщений
	 * 
	 * @throws SQLException
	 *             - ошибки при анализе
	 */
	public void messaging() {
		String msg = null;
		try {
			msg = in.readLine();
		} catch (IOException e) {
			System.err.println("Couldn't read client message");
			System.exit(-1);
		}
		if (msg != null) {
			System.out.println("Clent says: " + msg);
			try {
				messageProcessing(msg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * закрытие соединения с клиентом
	 */
	public void CloseServer() {
		// closing all thigs
		out.close();
		try {
			in.close();
		} catch (IOException e) {
			System.err.println("Couldn't close BufferedReader");
			System.exit(-1);
		}
		try {
			sockClient.close();
		} catch (IOException e) {
			System.err.println("Couldn't close ClientSocket");
			System.exit(-1);
		}
		try {
			sockServer.close();
		} catch (IOException e) {
			System.err.println("Couldn't close ServerSocket");
			System.exit(-1);
		}
	}

	/**
	 * Анализ входящих сообщений Ищем во входящей строке ключевое слово, если
	 * оно находится, запускаем соответсвующий метод
	 * 
	 * @param msg
	 *            - сообщение от клиента
	 * @throws SQLException
	 *             - ошибки БД
	 */
	public void messageProcessing(String msg) throws SQLException {
		System.out.println("Message processing...");
		String prefix = null;
		try {
			prefix = msg.substring(0, msg.indexOf(" "));
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Received message has wrong format.");
			return;
		}
		// checking words
		if (containsIgnoreCase(msg, "CAPABILITY")) {
			Capability capability = new Capability(prefix, out);
			capability.execute();
		} else if (containsIgnoreCase(msg, "NOOP")) {
			Noop noop = new Noop(prefix, folder, user.userId, out, db);
			noop.execute();
		} else if (containsIgnoreCase(msg, "LOGIN")) {
			user = new User();
			Login login = new Login(prefix, msg, out, user, this);
			login.execute();
		} else if (containsIgnoreCase(msg, "LIST")) {
			List list = new List(prefix, msg, user.userId, out, db);
			list.execute();
		} else if (containsIgnoreCase(msg, "LSUB")) {
			Lsub lsub = new Lsub(prefix, out);
			lsub.execute();
		} else if (containsIgnoreCase(msg, "CREATE")) {
			Create create = new Create(prefix, msg, user.userId, out, db);
			create.execute();
		} else if (containsIgnoreCase(msg, "SELECT")) {
			Select select = new Select(prefix, msg, user.userId, UIDVALIDITY, db, out, this);
			select.execute();
			UIDVALIDITY = select.getUIDVALIDITY();
			folder = select.getFolder();
		} else if (containsIgnoreCase(msg, "UID ")) {
			Uid uid = new Uid(prefix, msg, user.userId, folder, out, db, this);
			uid.execute();
		} else if (containsIgnoreCase(msg, "SEARCH")) {
			Search search = new Search(prefix, msg, user.userId, folder, out, db);
			search.execute();
		} else if (containsIgnoreCase(msg, "FETCH")) {
			Fetch fetch = new Fetch(prefix, msg, folder, user.userId, out, db, this);
			fetch.fetchExecute();
		} else if (containsIgnoreCase(msg, "SUBSCRIBE")) {
			Subscribe subscribe = new Subscribe(prefix, out);
			subscribe.execute();
		} else if (containsIgnoreCase(msg, "STATUS")) {
			Status status = new Status(prefix, msg, user.userId, out, db);
			status.execute();
		} else if (containsIgnoreCase(msg, "STORE")) {
			Store store = new Store(prefix, msg, db, out);
			store.execute();
		} else if (containsIgnoreCase(msg, "LOGOUT")) {
			Logout logout = new Logout(prefix, out);
			logout.execute();
			user = new User();
		}
		// if (client_message.contains(LOGIN[0]) ||
		// client_message.contains(LOGIN[1]) )
		// {
		// LoginCommand login = new LoginCommand(prefix, client_message,
		// assistant, out);
		// login.DoCommand();
		// UserId = login.UserId;
		// }
		//
		// if (client_message.contains(SELECT[0]) ||
		// client_message.contains(SELECT[1]) )
		// {
		// SelectCommand select = new SelectCommand(prefix, client_message,
		// assistant, out, UserId, UIDVALIDITY);
		// select.DoCommand();
		// folder = select.folder;
		// UIDVALIDITY = select.UIDVALIDITY;
		// }
		//
		// if (client_message.contains(SEARCH[0]) ||
		// client_message.contains(SEARCH[1]) )
		// {
		// SearchCommand search = new SearchCommand(prefix, client_message,
		// assistant, out, UserId);
		// search.DoCommand();
		// }
		//
		// if (client_message.contains(FETCH[0]) ||
		// client_message.contains(FETCH[1]) )
		// {
		// FetchCommand fetch = new FetchCommand(prefix, client_message,
		// assistant, out, folder, UserId);
		// fetch.DoCommand();
		// }
		//
		// if (client_message.contains(NOOP[0]) ||
		// client_message.contains(NOOP[1]) )
		// {
		// NoopCommand noop = new NoopCommand(prefix, out);
		// noop.DoCommand();
		// }
		//
		// if (client_message.contains(LOGOUT[0]) ||
		// client_message.contains(LOGOUT[1]) )
		// {
		// LogoutCommand logout = new LogoutCommand(prefix, out);
		// logout.DoCommand();
		// }
		//
		// if (client_message.contains(LSUB[0]) ||
		// client_message.contains(LSUB[1]) )
		// {
		// LsubCommand lsub = new LsubCommand(prefix, out);
		// lsub.DoCommand();
		// }
		//
		// if (client_message.contains(LIST[0]) ||
		// client_message.contains(LIST[1]) )
		// {
		// ListCommand list = new ListCommand(prefix, client_message, out,
		// assistant, UserId);
		// list.DoCommand();
		// }
		//
		// if (client_message.contains(CREATE[0]) ||
		// client_message.contains(CREATE[1]) )
		// {
		// CreateCommand create = new CreateCommand(prefix, client_message,
		// UserId, out, assistant);
		// create.DoCommand();
		// }
		//
		// if (client_message.contains(SUBSCRIBE[0]) ||
		// client_message.contains(SUBSCRIBE[1]) )
		// {
		// SubscribeCommand subscribe = new SubscribeCommand(prefix, out);
		// subscribe.DoCommand();
		// }
		//
		// if (client_message.contains(COPY[0]) ||
		// client_message.contains(COPY[1]) )
		// {
		// CopyCommand copy = new CopyCommand(prefix, client_message, assistant,
		// out);
		// copy.DoCommand();
		// }
		//
		// if (client_message.contains(STATUS[0]) ||
		// client_message.contains(STATUS[1]) )
		// {
		// StatusCommand status = new StatusCommand(prefix, out, client_message,
		// UserId, assistant);
		// status.DoCommand();
		// }
		//
		//
		// if (client_message.contains(STORE[0]) ||
		// client_message.contains(STORE[1]) )
		// {
		// StoreCommand store = new StoreCommand(prefix, client_message,
		// assistant, out);
		// store.DoCommand();
		// }

	}

	// /**
	// * Извлечения первого параметра из сообщения клиента
	// *
	// * @param request
	// * - сообщение клиента
	// * @return первый параметр
	// */
	// public String GetFirstParamFromRequest(String request) {
	// return request.substring((request.indexOf("\"") + 1),
	// request.indexOf("\"", request.indexOf("\"") + 1));
	// }
	//
	// /**
	// * Извлечения второго параметра из сообщения клиента
	// *
	// * @param request
	// * - сообщение клиента
	// * @return второй параметр
	// */
	// public String GetSecondParamFromRequest(String request) {
	// int indexSecondseparator = request.indexOf("\"", request.indexOf("\"") +
	// 1);
	// return request.substring(indexSecondseparator + 3, request.length() - 1);
	// }

	/**
	 * Открытие сообщения с диска
	 * 
	 * @param UID
	 *            - UID сообщения
	 * @return открытое сообщение
	 */
	public MimeMessage OpenMessage(String UID) {
		// Connect *.eml file
		String emlFile = "/Users/andrey/Desktop/Net/Messages/" + UID + ".eml";
		Properties props = System.getProperties();
		Session mailSession = Session.getDefaultInstance(props, null);
		InputStream source = null;
		try {
			source = new FileInputStream(emlFile);
		} catch (FileNotFoundException e) {
			System.err.println("Can't find this way ");
			e.printStackTrace();
		}
		MimeMessage message = null;
		try {
			message = new MimeMessage(mailSession, source);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return message;
	}

	/**
	 * Отправка сообщения клиенту и вывод его в консоль
	 * 
	 * @param response
	 *            - ответ для клиента
	 * @param out
	 *            - поток для общения с клиентом
	 */
	public void sendResponse(String response, PrintWriter out) {
		out.println(response);
		System.out.println("Server says: " + response);
	}

	public boolean loginUser(String login, String password) {
		for (User user : users) {
			if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
				System.out.println("The user was successfuly logged in.");
				return true;
			}
		}
		return false;
	}

	public boolean findUser(String login, String password) throws SQLException {
		// return _server.db.findUser(login, password);
		for (User user : users)
			if (user.getLogin().equals(login) && user.getPassword().equals(password))
				return true;
		return false;
	}

	public User getUser(String login, String password) throws SQLException {
		if (!findUser(login, password))
			return null;
		User user = null;
		ResultSet rs = db.getUser(login, password);
		if (rs.next())
			user = new User(rs.getInt("id"), rs.getString("login"), rs.getString("password"));
		return user;
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

	public static boolean containsIgnoreCase(String str, String searchStr) {
		if (str == null || searchStr == null)
			return false;

		final int length = searchStr.length();
		if (length == 0)
			return true;

		for (int i = str.length() - length; i >= 0; i--) {
			if (str.regionMatches(true, i, searchStr, 0, length))
				return true;
		}
		return false;
	}

}
