package masterServer;

import java.sql.SQLException;
import java.util.List;

import security.Authenticater;
import shared.dbo.Account;
import shared.dbo.AuthenticationToken;
import shared.dbo.GameServer;
import shared.dbo.GameServerInfo;
import shared.idal.IDataAccessLayer;

public class MasterServerRequestHandler {

	private IDataAccessLayer dal;

	public MasterServerRequestHandler(IDataAccessLayer dal) {
		this.dal = dal;
	}

	public List<GameServerInfo> getGameServerList(String token, String username) {
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
		if (!authenticated)
			return null;
		List<GameServerInfo> gameServers;
		try {
			gameServers = dal.getGameServerDAO().getGameServersForClients();
		} catch (SQLException e) {
			String error = "A client tried to retrieve game server list. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}
		return gameServers;
	}

	public String joinGameServer(String token, String username, String ip) {
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			String error = "A client tried to join a game server. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}
		AuthenticationToken authenticationToken;
		try {
			authenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			String error = "A client tried to join a game server. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}
		boolean authenticated = Authenticater.checkAuthenticationToken(authenticationToken, token);
		if (!authenticated)
			return null;

		try {
			dal.getAuthenticationTokenDAO().updateAuthenticationTokenField(account.getID(), AuthenticationToken.GAME_SERVER_IP, ip);
		} catch (SQLException e) {
			String error = "A client tried to join a game server. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}
		return ip;
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
		if (!authenticated)
			return null;
		boolean isHosting;
		try {
			isHosting = dal.getGameServerDAO().getGameServer(account.getID()) != null;
		} catch (SQLException e) {
			String error = "A client tried to host a game server. [Username: " + username + "] SQLException: " + e.getMessage();
			System.out.println(error);
			e.printStackTrace();
			return null;
		}

		if (isHosting) {
			try {
				dal.getGameServerDAO().updateGameServerField(account.getID(), GameServer.LAST_CHECKED, "DEFAULT");
			} catch (SQLException e) {
				String error = "A client tried to host a game server. [Username: " + username + "] SQLException: " + e.getMessage();
				System.out.println(error);
				e.printStackTrace();
				return null;
			}
		} else {
			try {
				dal.getGameServerDAO().createGameServer(account.getID());
			} catch (SQLException e) {
				String error = "A client tried to host a game server. [Username: " + username + "] SQLException: " + e.getMessage();
				System.out.println(error);
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

}
