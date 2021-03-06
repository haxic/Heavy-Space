package shared.dbo;

import java.time.LocalDateTime;

public class Account {
	public static final String ACCOUNT = "account";
	public static final String ID = "id";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String CREATED_DATE = "created_date";
	private int id;
	private String username;
	private String password;
	private LocalDateTime createdDate;

	public Account(int id, String username, String password, LocalDateTime createdDate) {
		super();
		this.id = id;
		this.username = username;
		this.username = username;
		this.password = password;
		this.createdDate = createdDate;
	}

	public int getID() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	@Override
	public String toString() {
		return "[" + id + ", " + username + ", " + password + ", " + createdDate + "]";
	}
}
