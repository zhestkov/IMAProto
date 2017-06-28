package architecture;

public class Mailbox {

	private int id = -1;
	private String name = null;
	private int userId = -1;
	private int parentId = -1;
	
	public Mailbox(int id, String name, int userId, int parentId) {
		this.id = id;
		this.name = name;
		this.userId = userId;
		this.parentId = parentId;
	}
	public int getParentId() {
		return parentId;
	}
	public String getName() {
		return name;
	}
	public int userId() {
		return userId;
	}
	public int getId() {
		return id;
	}
	public void setParentId(int parent) {
		parentId = parent;
	}
	
	
}
