package loginServer.dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import loginServer.dbo.AuthenticationToken;

public class AuthenticationTokenDAO implements IAuthenticationTokenDAO {

	private Connection dbc;

	public AuthenticationTokenDAO(Connection dbc) {
		this.dbc = dbc;
	}

	@Override
	public AuthenticationToken getAuthenticationToken(int accountID) throws SQLException {
		Statement s = dbc.createStatement();
		ResultSet rs = s.executeQuery("SELECT * FROM " + AuthenticationToken.AUTHENTICATION_TOKEN + " WHERE " + AuthenticationToken.ACCOUNT_ID + " = " + accountID + ";");
		AuthenticationToken authenticationToken = fillAuthenticationToken(rs);
		return authenticationToken;
	}

	@Override
	public void createAuthenticationToken(int accountID, String clientIP, String serverIP) throws SQLException {
		Statement s = dbc.createStatement();
		String sql = "INSERT INTO " + AuthenticationToken.AUTHENTICATION_TOKEN + " (" + AuthenticationToken.ACCOUNT_ID + ", " + AuthenticationToken.CLIENT_IP + ", "
				+ AuthenticationToken.MASTER_SERVER_IP + ") " + "VALUES ('" + accountID + "','" + serverIP + "','" + serverIP + "');";
		s.executeUpdate(sql);
	}

	@Override
	public void updateAuthenticationToken(int accountID, String clientIP, String masterServerIP) throws SQLException {
		Statement s = dbc.createStatement();
		String sql = "UPDATE " + AuthenticationToken.AUTHENTICATION_TOKEN + " SET";
		sql += " " + AuthenticationToken.CLIENT_IP + " = '" + clientIP + "'";
		sql += ", " + AuthenticationToken.MASTER_SERVER_IP + " = '" + masterServerIP + "'";
		sql += ", " + AuthenticationToken.AUTHENTICATION_DATE + " = DEFAULT";
		sql += " WHERE " + AuthenticationToken.ACCOUNT_ID + " = " + accountID + ";";
		s.executeUpdate(sql);
	}

	private AuthenticationToken fillAuthenticationToken(ResultSet rs) throws SQLException {
		while (rs.next()) {
			int id = rs.getInt(AuthenticationToken.ACCOUNT_ID);
			String username = rs.getString(AuthenticationToken.CLIENT_IP);
			String password = rs.getString(AuthenticationToken.MASTER_SERVER_IP);
			Date createdDate = rs.getDate(AuthenticationToken.AUTHENTICATION_DATE);
			AuthenticationToken authenticationToken = new AuthenticationToken(id, username, password, createdDate);
			return authenticationToken;
		}
		return null;
	}
}
