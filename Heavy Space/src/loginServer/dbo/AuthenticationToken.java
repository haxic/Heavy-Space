package loginServer.dbo;

import java.sql.Date;

public class AuthenticationToken {
	public static final String AUTHENTICATION_TOKEN = "authentication_token";
	public static final String ACCOUNT_ID = "account_id";
	public static final String CLIENT_IP = "client_ip";
	public static final String MASTER_SERVER_IP = "master_server_ip";
	public static final String AUTHENTICATION_DATE = "authentication_date";
	private int accountID;
	private String clientIP;
	private String serverIP;
	private Date authenticationDate;

	public AuthenticationToken(int accountID, String clientIP, String serverIP, Date authenticationDate) {
		this.accountID = accountID;
		this.clientIP = clientIP;
		this.serverIP = serverIP;
		this.authenticationDate = authenticationDate;
	}

	public int getAccountID() {
		return accountID;
	}

	public String getClientIP() {
		return clientIP;
	}

	public String getServerIP() {
		return serverIP;
	}

	public Date getAuthenticationDate() {
		return authenticationDate;
	}
	
	@Override
	public String toString() {
		return "[" + accountID + ", " + clientIP + ", " + serverIP + ", " + authenticationDate + "]";
	}
}
