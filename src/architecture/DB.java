package architecture;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class DB {
	// public String DB_URL =
	// "jdbc:firebirdsql://localhost:3050/C:/IMAP4/DB.FDB";
	// public String DB_DEFAULT_USER = "SYSDBA";
	// public String DB_DEFAULT_PASSWORD = "masterkey";
	// public String DB_DEFAULT_ENCODING = "win1251";
	public Connection conn = null;
	PreparedStatement statement = null;
	Properties props = null;
	private Server _server;
	private IncomingServer incServer;

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:3306/IMAP";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "";
	// /usr/local/var/mysql/
	// Mysql@127.0.0.1:3306

	public DB(Server serverInstance) {
		this._server = serverInstance;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		connect();
	}

	public DB(IncomingServer serverInstance) {
		this.incServer = serverInstance;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		connect();
	}

	public void connect() {
		System.out.println("Connecting to database...");
		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet selectAll() throws SQLException {
		String query = "select * from User";
		statement = conn.prepareStatement(query);
		ResultSet res = statement.executeQuery();
		return res;
	}

	public ResultSet findUser(String login, String password) throws SQLException {
		String query = "select * as result from User where login =\"" + login + "\" and password=\"" + password + "\"";
		statement = conn.prepareStatement(query);
		ResultSet res = statement.executeQuery();
		return res;
		// return (res.next() && res.getInt("result") == 1) ? true : false;
	}

	public ResultSet getUser(String login, String password) throws SQLException {
		String query = "select * from User where login =\"" + login + "\" and password=\"" + password + "\"";
		statement = conn.prepareStatement(query);
		ResultSet res = statement.executeQuery();
		return res;
	}

	public ArrayList<User> getUsers() throws SQLException {
		ArrayList<User> users = new ArrayList<User>();
		String query = "select * from User";
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			users.add(new User(rs.getInt("id"), rs.getString("login"), rs.getString("password")));
		}
		return users;
	}

	public ArrayList<Mailbox> getFolders() throws SQLException {
		ArrayList<Mailbox> folders = new ArrayList<Mailbox>();
		String query = "select * from Mailbox";
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			folders.add(
					(new Mailbox(rs.getInt("id"), rs.getString("name"), rs.getInt("User_id"), rs.getInt("parentId"))));
		}
		return folders;
	}

	public ResultSet getAllFoldersByUser(int userId) throws SQLException {
		String query = "select * from Mailbox where User_id = " + userId;
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		return rs;
	}

	public ResultSet getInnerFolders(String rootFolder, int userId) throws SQLException {
		int parentId = getFolderId(rootFolder, userId);
		if (parentId == -1) {
			System.out.println("rootFolder was not found.");
			return null;
		}
		String query = "select * from Mailbox where parentId = " + parentId + " and User_id = " + userId;
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		return rs;

	}

	public int firstUnseen(int userId, String folder) throws SQLException {
		String query = "select id from Message where flagSeen = 0" + " and Mailbox_id = ("
				+ "select id from Mailbox where Mailbox.name = \"" + folder + "\"" + " and Mailbox.User_id = " + userId
				+ ") and Message.id = (select max(Message.id) from Message)";
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (rs.next())
			return rs.getInt("id");
		else
			return 0;
	}
	
	public int countUnseentMessages(int userId, String folder) throws SQLException {
		String query = "select count(1) as amount from Message where Mailbox_id ="
				+ "(select id from Mailbox where Mailbox.name = \"" + folder + "\" and Mailbox.User_id =" + userId
				+ ") and flagSeen = 0";
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (rs.next())
			return rs.getInt("amount");
		return 0;
	}

	public int countExistMessages(int userId, String folder) throws SQLException {
		String query = "select count(1) as amount from Message where Mailbox_id ="
				+ "(select id from Mailbox where Mailbox.name = \"" + folder + "\" and Mailbox.User_id =" + userId
				+ ")";
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (rs.next())
			return rs.getInt("amount");
		return 0;
	}

	public int countRecentMessages(int userId, String folder) throws SQLException {
		String query = "select count(1) as amount from Message where flagRecent = 1 and Mailbox_id = "
				+ "(select id from Mailbox where Mailbox.name = \"" + folder + "\" and Mailbox.User_id = " + userId
				+ ")";
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (rs.next())
			return rs.getInt("amount");
		return 0;
	}
	
	public ResultSet countFlaggedMessage(int userId, String folder, String flag, int flagValue) {
		int folderId = -1;
		try {
			folderId = getFolderId(folder, userId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (folderId == -1) {
			System.out.println("countFlaggedMessage:folderId was not found.");
			return null;
		}
		String query = "select id from Message where User_id = " + userId
				+ " and Mailbox_id = " + folderId + " and flag" + flag + " = " + flagValue;
		try {
			statement = conn.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			return rs;
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getNextMessageUID(int userId, String folder) throws SQLException {
		int uidNext = 1;
		String query = "select max(Message.id) as maxId from Message inner join Mailbox on Message.Mailbox_id = Mailbox.id"
				+ " where Mailbox.id = (select id from Mailbox where User_id = " + userId + " and name = \""
				+ folder + "\" )";
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (rs.next())
			uidNext = rs.getInt("maxId") + 1;
		return uidNext;
	}

	public int createFolder(String folder, int userId, int parentId) throws SQLException {
		String insertQuery = "insert into Mailbox (name, User_id, parentId) values (?, ?, ?)";
		statement = conn.prepareStatement(insertQuery);
		statement.setString(1, folder);
		statement.setInt(2, userId);
		statement.setInt(3, parentId); // TO DO: add support for inner folders
		//
		int res = statement.executeUpdate(); // returns either
		// the row count for SQL DML statements or 0 for SQL statements that
		// return nothing
		return res;
		// updating ArrayList<Mailbox> in Server
	}

	public boolean isFolderExist(String folder, int userId) throws SQLException {
		String query = "select count(1) as amount from Mailbox where name = \"" + folder + "\"" + " and User_id = "
				+ userId;
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (rs.next() && rs.getInt("amount") != 0)
			return true;
		return false;
	}

	public int getFolderId(String folder, int userId) throws SQLException {
		String query = "select id from Mailbox where name = \"" + folder + "\" and User_id = " + userId;
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (rs.next())
			return rs.getInt("id");
		return -1;
	}

	public int updateFolderParentId(int folderId, int parentId) throws SQLException {
		String query = "update Mailbox set parentId = ? where id = ?";
		statement = conn.prepareStatement(query);
		statement.setInt(1, parentId);
		statement.setInt(2, folderId);
		return statement.executeUpdate();
	}

	public ResultSet getMessageFlags(int userId, String folder, String uID) throws SQLException {
		String query = "select flagSeen as SEEN, flagAnswered as ANSWERED, flagDeleted as DELETED,"
				+ "flagDraft as DRAFT, flagRecent as Recent from Message";
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		return rs;
	}
	
	public int addFlagToMessage(String msg_id, String flag) {
		String query = "update Message set flag" + flag
				+ " = 1 where id = " + msg_id;
		try {
			statement = conn.prepareStatement(query);
			return statement.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	public int removeFlagFromMessage(String msg_id, String flag) {
		String query = "update Message set flag" + flag 
				+ " = 0 where id = " + msg_id;
		try {
			statement = conn.prepareStatement(query);
			return statement.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	// проверка прихода новых сообщений (для команды NOOP)
	public int getNewMsgFlag(int userId) throws SQLException {
		String query = "select update_status from User where id = " + userId;
		statement = conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (rs.next())
			return rs.getInt("update_status");
		return 0;
	}
	public int resetNewMsgFlag(int userId) throws SQLException {
		String query = "update User set update_status = ? where id = ?";
		statement = conn.prepareStatement(query);
		statement.setInt(1, 0);
		statement.setInt(2, userId);
		return statement.executeUpdate();
	}

}
