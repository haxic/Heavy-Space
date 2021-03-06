package shared.idal;

import java.sql.SQLException;

import shared.dbo.AuthenticationToken;

public interface IAuthenticationTokenDAO {
	public AuthenticationToken getAuthenticationToken(int accountID) throws SQLException;
	public void createAuthenticationToken(int accountID, String clientIP, String serverIP) throws SQLException;
	public void updateAuthenticationToken(int accountID, String clientIP, String masterServerIP) throws SQLException;
	public void updateAuthenticationTokenField(int id, String field, Object value) throws SQLException;
}
