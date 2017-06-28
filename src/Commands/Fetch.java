package Commands;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import architecture.ClientHandler;
import architecture.DB;
import architecture.IncomingServer;

public class Fetch extends Command {
	private PrintWriter out = null;
	private String request = null;
	private DB db;
	private int userId = -1;
	private String folder = null;
	private String startUID = null; // startUID:
	private String endUID = null; // :endUID
	private boolean isSequence = false; // startUID:endUID
	private boolean isAllUID = false; // startUID:*
	private boolean isUIDVector = false;

	public Fetch(String prefix, String request, String folder, int userId, PrintWriter out, DB db,
			ClientHandler handler) {
		this.prefix = prefix;
		this.request = request;
		this.folder = folder;
		this.userId = userId;
		this.out = out;
		this.db = db;
		this._handler = handler;
	}
	
	public Fetch(String prefix, String request, String folder, int userId, PrintWriter out, DB db,
			IncomingServer incServer) {
		this.prefix = prefix;
		this.request = request.toUpperCase();
		this.folder = folder;
		this.userId = userId;
		this.out = out;
		this.db = db;
		this._server = incServer;
	}

	public void execute() {
		startUID = "";
		endUID = "";
		isSequence = false;
		isAllUID = false;
		isUIDVector = false;
		Vector<String> vecUID = new Vector<String>();
		int sep = request.indexOf(':');
		if (sep > 0) { // we have sequence
			isSequence = true;
			sep--;
			while (request.charAt(sep) != ' ') { // before ':'
				startUID = request.charAt(sep) + startUID;
				sep--;
			}

			sep = request.indexOf(':') + 1;
			if (request.charAt(sep) == '*') // if all UIDs
				isAllUID = true;
			else {
				while (request.charAt(sep) != ' ') { // after ':'
					endUID += request.charAt(sep); // direction!
					sep++;
				}
			}
		} else { // 1 or a few
			sep = request.indexOf(',');
			if (sep <= 0) { // 1
				sep = request.indexOf('H') + 2;  // FETCH__ (warn: could be fetch__)
				while (request.charAt(sep) != ' ') {
					startUID += request.charAt(sep);
					sep++;
				}
			} else { // a few
				isUIDVector = true;
				String tempUID = "";
				sep--;
				while (request.charAt(sep) != ' ') {
					tempUID = request.charAt(sep) + tempUID;
					sep--;
				}
				vecUID.add(tempUID);
				sep = request.indexOf(',') + 1;
				tempUID = "";
				while (request.charAt(sep) != ' ') {
					if (request.charAt(sep) != ',') {
						tempUID += request.charAt(sep);
					} else {
						vecUID.add(tempUID);
						tempUID = "";
					}
					sep++;
				}
				vecUID.add(tempUID);
			}
		}
		if (isSequence) { // sequence: {A:B} or {A:*} (till the end)
			int startuid = Integer.valueOf(startUID);
			int amount = 0;
			try {
				amount = db.countExistMessages(userId, folder);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			int cnt = 0;
			if (amount == 0) {
				System.out.println("no exist messages");
				return;
			}
			cnt = amount;
			if (isAllUID) {
				if (cnt <= 0) {
					sendResponse(prefix + " NO FETCH no one message", out);
					return;
				}
				for (int i = startuid; i <= cnt; i++) {
					try {
						genAndSendFetchResponse(request, Integer.toString(i), isSequence);
					} catch (SQLException e) {
						System.out.println("genAndSendFetchResponse error, i = " + i);
						e.printStackTrace();
					}
				}
			} else {
				int enduid = Integer.parseInt(endUID);
				int finishUID = 0;
				if (enduid <= cnt)
					finishUID = enduid;
				else {
					sendResponse(prefix + " NO FETCH in folder only " + cnt + " messages", out);
					return;
				}
				for (int i = startuid; i <= finishUID; i++) {
					try {
						genAndSendFetchResponse(request, Integer.toString(i), isSequence);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		} else { // Vector e.g. {1,5,7} or single message
			if (isUIDVector) { // vector
				for (int i = 0; i < vecUID.size(); i++) {
					try {
						genAndSendFetchResponse(request, vecUID.get(i), isSequence);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else { // single
				try {
					genAndSendFetchResponse(request, startUID, isSequence);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void fetchExecute() {
		execute();
		sendResponse(prefix + " OK FETCH completed\r\n", out);
	}

	public void genAndSendFetchResponse(String request, String UID, boolean sequence) throws SQLException {
		// request - строка запроса
		// sequence - если сообщений несколько
		boolean needBracket = true;
		int sep = 10; // this number is roughly, start index for indexOf
		//MimeMessage msg = _handler.OpenMessage(UID); // multi
		MimeMessage msg = _server.OpenMessage(UID); // single
		File file = new File("/Users/andrey/Desktop/Net/Messages" + UID + ".eml");
		long fileLength = file.length();
		
		String response = "* " + UID + " FETCH (";
		if (sequence)
			response += " UID " + UID + " "; // ???
		
		// if RFC822.SIZE command in Fetch
		if (request.indexOf("RFC822.SIZE") != -1)
			try {
				response += "RFC822.SIZE " + fileLength + " ";
				//response += "RFC822.SIZE " + msg.getSize() + " ";
			} catch (Exception e) {
				e.printStackTrace();
			}

		// if INTERNALDATE command in Fetch
		// INTERNALDATE = sendDate but in real = receiveDate
		if (request.indexOf("INTERNALDATE") != -1)
			try {
				response += "INTERNALDATE \"" + msg.getSentDate() + "\" ";
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		// if FLAGS command in Fetch
		if (request.contains("FLAGS")) {
			ResultSet rs = db.getMessageFlags(userId, folder, UID);
			String space = "";
			if (rs.next()) {
				response += "FLAGS (";
				if ((Integer.parseInt(rs.getString("SEEN"))) == 1) {
					response += "\\Seen";
					space = " ";
				}
//				if ((Integer.parseInt(rs.getString("SEEN"))) == 0) {
//					response += "\\Unseen";
//					space = " ";
//				}
				if ((Integer.parseInt(rs.getString("ANSWERED"))) == 1) {
					response += space + "\\Answered";
					space = " ";
				}
				if ((Integer.parseInt(rs.getString("DELETED"))) == 1) {
					response += space + "\\Deleted";
					space = " ";
				}
				if ((Integer.parseInt(rs.getString("DRAFT"))) == 1) {
					response += space + "\\Draft";
					space = " ";
				}
				if ((Integer.parseInt(rs.getString("Recent"))) == 1) {
					response += space + "\\Recent";
					space = " ";
				}
				response += ") ";
			}
		}
		
		// if UID command in Fetch
		if (request.indexOf("UID") != -1 && !sequence)
			response += "UID " + UID + " ";

		// if ENVELOPE command in Fetch
		if (request.indexOf("ENVELOPE") != -1) {
			String envelope = null;
			try {
				envelope = getEnvelope(msg);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			response += envelope + " ";
		}

		// If BODYSTRUCTURE/ENVELOPE command in Fetch
		if (request.indexOf("BODYSTRUCTURE") != -1) {
			String bodyStructure = null;
			try {
				bodyStructure = getBodyStructure(msg);
				bodyStructure.toUpperCase();
			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			response += bodyStructure + " "; // + space?
		}

		if ((request.indexOf("BODY.PEEK[HEADER.FIELDS") != -1) || request.indexOf("BODY.PEEK[]") != -1) {
			needBracket = false;
			//String emlFile = "C:\\Net\\Messages\\" + UID + ".eml";
//			String strLine = null;
//			String lastLine = null;
//			String line = "";
//			try {
//				// Open the file (first command line parameter)
//				FileInputStream fstream = new FileInputStream(emlFile);
//				// get the object of DataInputStream
//				DataInputStream in = new DataInputStream(fstream);
//				BufferedReader br = new BufferedReader(new InputStreamReader(in));
//				// Read file line by line
//				while ((strLine = br.readLine()) != null) {
//					line += strLine + "\r\n";
//					lastLine = strLine;
//					// print the content on the console
//				}
//				in.close();
//				//System.out.println(line);
//			} catch (Exception e) { // catch exception if any
//				System.err.println("Error: " + e.getMessage());
//			}

			//response += "BODY.PEEK[] {" + line.length() + "} \r\n";
			String line = null;
			try {
				Scanner sc = new Scanner(file);
				response += "BODY[] {" + fileLength + "}\r\n";
				while (sc.hasNextLine()) {
					line = sc.nextLine() + "\r\n";
					response += line;
				}
				
			} catch (FileNotFoundException e1) {
				System.out.println("Message file doesn't exist.");
				e1.printStackTrace();
			}
			
//			FileInputStream fstream = new FileInputStream(emlFile);
//			DataInputStream in = new DataInputStream(fstream);
//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			//response += "BODY[] {" + line.length() + "} \r\n";
//			try {
//				InternetAddress[] addrFrom = (InternetAddress[]) msg.getFrom();
//				if (addrFrom != null) {
//					response += "From: ";
//					String space = "";
//					for (int i = 0; i < addrFrom.length; i++) {
//						response += space + addrFrom[i].getPersonal() + " <" + addrFrom[i].getAddress() + ">";
//						space = ", ";
//					}
//					response += "\r\n";
//
//				}
//
//				InternetAddress[] addrTo = (InternetAddress[]) msg.getRecipients(MimeMessage.RecipientType.TO);
//				if (addrTo != null) {
//					response += "To: ";
//					String space = "";
//					for (int i = 0; i < addrTo.length; i++) {
//						response += addrTo[i].getPersonal() + " <" + addrTo[i].getAddress() + ">";
//						space = ", ";
//					}
//					response += "\r\n";
//				}
//
//				InternetAddress[] addrCc = (InternetAddress[]) msg.getRecipients(MimeMessage.RecipientType.CC);
//				if (addrCc != null) {
//					response += "Cc: ";
//					String space = "";
//					for (int i = 0; i < addrCc.length; i++) {
//						response += space + addrCc[i].getPersonal() + " " + addrCc[i].getAddress();
//						space = ", ";
//					}
//					response += "\r\n";
//				}
//
//				InternetAddress[] addrBcc = (InternetAddress[]) msg.getRecipients(MimeMessage.RecipientType.BCC);
//				if (addrBcc != null) {
//					response += "Bcc: ";
//					String space = "";
//					for (int i = 0; i < addrBcc.length; i++) {
//						response += space + addrBcc[i].getPersonal() + " " + addrBcc[i].getAddress();
//						space = ", ";
//					}
//					response += "\r\n";
//				}
//
//				response += "Subject: " + msg.getSubject() + "\r\n";
//				response += "Date: " + msg.getSentDate() + "\r\n";
//				response += "Message-ID: " + msg.getMessageID() + "\r\n";
//				response += "Content-Type: " + msg.getContentType() + "\r\n";
//				
//				//response += line + "\r\n";
//				
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}

		if (request.indexOf("BODY[]") != -1) {
			needBracket = false;
			String emlFile = "/Users/andrey/Desktop/Net/Messages" + UID + ".eml";
			String strLine = null;
			String line = "";
			try {
				FileInputStream fstream = new FileInputStream(emlFile);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				while ((strLine = br.readLine()) != null) {
					line += strLine + "\n";
				}
				in.close();
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
			response += "BODY[]{" + line.length() + "}\r\n";
			response += line + "\n";
		}
		response += ")\r\n";
		sendResponse(response, out);
	}

	public String getEnvelope(MimeMessage msg) throws MessagingException {
		String envelope = "ENVELOPE (\"" + msg.getSentDate() + "\" " // date
				+ "\"" + msg.getSubject() + "\" "; // subject
		// from
		InternetAddress[] addrFrom = (InternetAddress[]) msg.getFrom();
		String from = parseAddrArray(addrFrom);
		envelope += from + " ";
		
		// sender, same since localhost
		envelope += from + " ";

		// reply-to
		InternetAddress[] addrReplyTo = (InternetAddress[]) msg.getReplyTo();
		String replyTo = parseAddrArray(addrReplyTo);
		envelope += replyTo + " ";

		// to
		InternetAddress[] addrTo = (InternetAddress[]) msg.getRecipients(MimeMessage.RecipientType.TO);
		String to = parseAddrArray(addrTo);
		envelope += to + " ";

		// cc
		InternetAddress[] addrCc = (InternetAddress[]) msg.getRecipients(MimeMessage.RecipientType.CC);
		String cc = parseAddrArray(addrCc);
		if (cc != null)
			envelope += cc + " ";
		else
			envelope += " NIL ";

		// bcc
		InternetAddress[] addrBcc = (InternetAddress[]) msg.getRecipients(MimeMessage.RecipientType.BCC);
		String bcc = parseAddrArray(addrBcc);
		if (bcc != null)
			envelope += bcc + " ";
		else
			envelope += " NIL ";

		// in-reply-to ?
		envelope += " NIL ";

		envelope += " \"" + msg.getMessageID() + "\")";
		return envelope;
	}

	public String parseAddress(InternetAddress addr) {
		String line = "";
		String person;
		String address;
		String name, host;
		try {
			person = addr.getPersonal();
			if (person == null)
				person = " NIL ";
			else
				person = " \"" + person + "\" ";
		} catch (NullPointerException e) {
			person = " NIL ";
		}

		try {
			address = addr.getAddress();
			address = " \"" + address + "\" ";
		} catch (NullPointerException e) {
			address = " NIL ";
		}

		try {
			name = address.substring(0, address.indexOf('@'));
			name = name + "\"";
		} catch (StringIndexOutOfBoundsException e) {
			name = " NIL ";
		}

		try {
			host = address.substring(address.indexOf('@') + 1, address.length());
			host = " \"" + host;
		} catch (StringIndexOutOfBoundsException e) {
			host = " NIL ";
		}
		line = "(" + person + "NIL" + name + " " + host + ")";

		return line;
	}

	public String parseAddrArray(InternetAddress[] addr) {
		String addressLine = "(";
		String from = "";
		try {
			if (addr.length >= 0) {
				String line = "";
				for (int i = 0; i < addr.length; i++) {
					from = parseAddress(addr[i]);
					addressLine += from;
				}
			} else
				addressLine += "(NIL NIL NIL NIL)";
			addressLine += ")";
		} catch (NullPointerException e) { // if field is empty
			System.out.println("Field is empty");
			return null;
		}
		return addressLine;
	}

	public String getBodyStructure(MimeMessage msg) throws MessagingException, IOException {
		String contentType = msg.getContentType();
		String res = "BODY (\"";
		int nextIndex = 0;

		// get params
		for (int i = 0; i < contentType.length(); i++) {
			if (contentType.charAt(i) != '/') {
				if (contentType.charAt(i) == ';') {
					i += 2;
					res += "\"";
					nextIndex = i;
					break;
				} else
					res += contentType.charAt(i);
			} else
				res += "\" \"";
		}

		res += " (\"";

		for (int i = nextIndex; i < contentType.length(); i++) {
			if (contentType.charAt(i) == '=')
				res += "\" \"";
			else
				res += contentType.charAt(i);
		}
		res += "\") NIL NIL \"7BIT\" "; // 7bit

		int octetSize = (msg.getSize() + 7) / 8;
		// counting lines
		String mes = msg.getContent().toString();
		int linesAmount = 0;
		for (int i = 0; i < mes.length(); i++) {
			if (mes.indexOf('\n', i) > 0) {
				i = mes.indexOf('\n', i);
				linesAmount++;
			}
		}
		res += octetSize + " " + linesAmount + ")";
		return res;
	}

}
