package architecture;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import Commands.*;

public class ClientHandler extends Thread {
	private Socket sockClient;
	public Server _server;
	public BufferedReader in = null;
	public PrintWriter out = null;
	public int userId = -1;
	public int UIDVALIDITY = 0;
	public String folder = null;
	public User user = null;
	

	public Socket getClientSocket() {
		return sockClient;
	}

	public ClientHandler(Socket sockClient, Server serverInstance) {
		this.sockClient = sockClient;
		this._server = serverInstance;
		user = new User();
		// setup the buffers
		try {
			// in = new BufferedReader(new
			// InputStreamReader(sockClient.getInputStream()));
			in = new BufferedReader(new InputStreamReader(new DataInputStream(sockClient.getInputStream())));
			out = new PrintWriter(sockClient.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println("Couldn't make buffer");
			System.exit(-1);
		}
	}

	public void run() {

		// Messaging...
		System.out.println("2: " + sockClient);
		System.out.println("Starting messaging...");

		while (true) {
			try {
				if (!messaging()) {
					try {
						in.close();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Thread.currentThread().interrupt();
					System.out.println("ClientHandler has been closed.");
					return;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean messaging() throws SQLException {
		String msg = null;
		// test DB
		try {
			ResultSet rs = _server.db.selectAll();
//			while (rs.next()) {
//				int id = rs.getInt("id");
//				String login = rs.getString("login");
//				String pass = rs.getString("password");
//				int access = rs.getInt("accessLevel");
//				boolean status = rs.getBoolean("status");
//				System.out.print("ID: " + id);
//				System.out.print(", Login: " + login);
//				System.out.print(", Password: " + pass);
//				System.out.print(", accessLevel: " + access);
//				System.out.println(", status: " + status);
//			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {
			System.out.println("test1");
			msg = in.readLine();
			System.out.println("test2");
		} catch (IOException e) {
			System.out.println("Couldn't read message from client");
			return false;
		}
		if (msg != null) {
			System.out.println("Received msg from client: " + msg);
			messageProcessing(msg);
		} else {
			System.out.println("in.readLine():Message = null");
			return false;
		}
		return true;
	}

	private void messageProcessing(String msg) throws SQLException {
		System.out.println("Message processing...");
		String prefix = null;
		try {
			prefix = msg.substring(0, msg.indexOf(" "));
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Received message has wrong format.");
		}
		if (containsIgnoreCase(msg, "CAPABILITY")) {
			Capability capability = new Capability(prefix, out);
			capability.execute();
		}
		else if (containsIgnoreCase(msg, "LOGIN")) {
			Login login = new Login(prefix, msg, out, this);
			login.execute();
			userId = login.getUserId();

		}
		else if (containsIgnoreCase(msg, "NOOP")) {
			Noop noop = new Noop(prefix, folder,  user.userId, out, _server.db);
			noop.execute();
		}
		else if (containsIgnoreCase(msg, "SELECT")) {
			Select select = new Select(prefix, msg, userId, UIDVALIDITY, _server.db, out, this);
			select.execute();
			UIDVALIDITY = select.getUIDVALIDITY();
			folder = select.getFolder();
		}
		else if (containsIgnoreCase(msg, "FETCH")) {
			Fetch fetch = new Fetch(prefix, msg, folder, userId, out, _server.db, this);
			fetch.execute();
		}
		else if (containsIgnoreCase(msg, "LIST")) {
			
		}
		else if (containsIgnoreCase(msg, "CREATE")) {
			Create create = new Create(prefix, msg, userId, out, _server.db);
			create.execute();
		}
		else if (containsIgnoreCase(msg, "DELETE")) {
			// TO DO
		}
		else if (containsIgnoreCase(msg, "SUBSCRIBE")) {
			// TO DO
		}
		else if (containsIgnoreCase(msg, "UNSUBSCRIBE")) {
			// TO DO
		}
		else if (containsIgnoreCase(msg, "LSUB")) {
			// TO DO
		}

	}

	public boolean findUser(String login, String password) throws SQLException {
		//return _server.db.findUser(login, password);
		for (User user : _server.users)
			if (user.getLogin().equals(login) && user.getPassword().equals(password))
				return true;
		return false;
	}

	public User getUser(String login, String password) throws SQLException {
		if (!findUser(login, password))
			return null;
		User user = null;
		ResultSet rs = _server.db.getUser(login, password);
		if (rs.next())
			user = new User(rs.getInt("id"), rs.getString("login"), rs.getString("password"));
		return user;
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
	
	public MimeMessage OpenMessage(String UID) {
		// Connect *.eml file
		String emlFile = "C:\\IMAP4\\mail\\" + UID + ".eml";
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

}
