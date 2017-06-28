package architecture;

import java.util.Date;

public class Message {
	// date, subject, from, sender, reply-to, to, cc, bcc,
    // in-reply-to, and message-id
	private Date date = null;
	private String subject = null;
	private String from = null;
	private String sender = "";
	private String replyTo = "";
	private String to = null;
	private String cc = "";
	private String bcc = "";
	private String inReplyTo = "";
	private String messageId = null;
	private String content = null;
	
	public Message(Date date, String subject, String from, String to, String messageId, String content) {
		this.date = date;
		this.subject = subject;
		this.from = from;
		this.to = to;
		this.messageId = messageId;
		this.content = content;
	}
	public Message(/* ... */) {
		
	}
	public Date getDate() {
		return date;
	}
	public String getSubject() {
		return subject;
	}
	public String getFrom() {
		return from;
	}
	public String getTo() {
		return to;
	}
	public String getMessageId() {
		return messageId;
	}
	public String getContent() {
		return content;
	}
	
}
