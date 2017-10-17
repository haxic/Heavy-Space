package loginServer.dal;

import java.sql.SQLException;

import loginServer.dbo.AuthenticationToken;

public interface IAuthenticationTokenDAO {
	public AuthenticationToken getAuthenticationToken(int accountID) throws SQLException;
	public void createAuthenticationToken(int accountID, String clientIP, String serverIP) throws SQLException;
	public void updateAuthenticationToken(int accountID, String clientIP, String serverIP) throws SQLException;
}
