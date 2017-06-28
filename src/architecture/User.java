package architecture;

import java.net.Socket;

public class User {
	public int userId = -1;
	private String login = null;
	private String password = null;
	private int state = 0;
	/*
	 * STATE: 0 - non-auth-ed / not logged in 1 - auth-ed / logged in 2 -
	 * selected
	 */


	public User() {
		
	}

	public User(int userId, String login, String password) {

		this.userId = userId;
		this.login = login;
		this.password = password;
		this.state = 0; // non-authed // not logged in

	}

	public int getId() {
		return userId;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public int getState() {
		return state;
	}

	public void setId(int user_id) {
		this.userId = user_id;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setState(int new_state) {
		state = new_state;
	}

	public String toString() {
		return "ID: " + getId() + ", login: " + getLogin() + ", password: " + getPassword() + ", state: " + getState();
	}

}
