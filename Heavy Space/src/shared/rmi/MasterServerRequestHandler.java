package shared.rmi;

import java.sql.SQLException;
import java.util.List;

import security.Authenticater;
import shared.GameServerInfo;
import shared.dal.IDataAccessLayer;
import shared.dbo.Account;
import shared.dbo.AuthenticationToken;

public class MasterServerRequestHandler {

	private IDataAccessLayer dal;

	public MasterServerRequestHandler(IDataAccessLayer dal) {
		this.dal = dal;
	}

	public List<GameServerInfo> getGameServerList(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	public String joinGameServer(String token, String ip) {
		// TODO Auto-generated method stub
		return null;
	}

	public String hostGameServer(String token, String username) {
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			String error = "A client tried to host a game server. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}
		AuthenticationToken authenticationToken;
		try {
			authenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			String error = "A client tried to host a game server. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}
		boolean authenticated = Authenticater.checkAuthenticationToken(authenticationToken, token);
		String ip = authenticationToken.getClientIP();
		return null;
	}

}
