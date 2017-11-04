package masterServer;

import java.sql.SQLException;
import java.util.List;

import security.Authenticater;
import shared.Config;
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
		// Validate client
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		AuthenticationToken authenticationToken;
		try {
			authenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		boolean authenticated = Authenticater.checkAuthenticationToken(authenticationToken, token);
		if (!authenticated)
			return null;

		// Get game server list
		List<GameServerInfo> gameServers;
		try {
			gameServers = dal.getGameServerDAO().getGameServersForClients();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return gameServers;
	}

	public String joinGameServer(String token, String username, String ip) {
		// Validate client
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		AuthenticationToken authenticationToken;
		try {
			authenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		boolean authenticated = Authenticater.checkAuthenticationToken(authenticationToken, token);
		if (!authenticated)
			return null;

		// Update game server ip on token
		try {
			dal.getAuthenticationTokenDAO().updateAuthenticationTokenField(account.getID(), AuthenticationToken.GAME_SERVER_IP, ip);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return ip;
	}

	public String hostGameServer(String token, String username) {
		// Validate client
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		AuthenticationToken authenticationToken;
		try {
			authenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		boolean authenticated = Authenticater.checkAuthenticationToken(authenticationToken, token);
		if (!authenticated)
			return null;

		// Check if the game server is already hosting
		boolean isHosting;
		try {
			isHosting = dal.getGameServerDAO().getGameServer(account.getID()) != null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		if (isHosting) {
			// If hosting, update game server information
			try {
				dal.getGameServerDAO().updateGameServerField(account.getID(), GameServer.LAST_CHECKED, "DEFAULT");
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			// If not hosting, create game server
			try {
				dal.getGameServerDAO().createGameServer(account.getID());
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public boolean checkClient(String token, String username, String clientToken, String clientUsername) {
		// Validate game server client
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		if (account == null)
			return false;
		AuthenticationToken authenticationToken;
		try {
			authenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		if (authenticationToken == null)
			return false;
		boolean authenticated = Authenticater.checkAuthenticationToken(authenticationToken, token);
		if (!authenticated)
			return false;

		// Validate client
		Account clientAccount;
		try {
			clientAccount = dal.getAccountDAO().getAccount(clientUsername);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		if (clientAccount == null)
			return false;
		AuthenticationToken clientAuthenticationToken;
		try {
			clientAuthenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(clientAccount.getID());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		if (clientAuthenticationToken == null)
			return false;
		boolean clientAuthenticated = Authenticater.checkAuthenticationToken(clientAuthenticationToken, clientToken);
		if (!clientAuthenticated)
			return false;
		return true;
	}

	public String heartbeat(String token, String username) {
		// Validate client
		Account account;
		try {
			account = dal.getAccountDAO().getAccount(username);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		AuthenticationToken authenticationToken;
		try {
			authenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		boolean authenticated = Authenticater.checkAuthenticationToken(authenticationToken, token);
		if (!authenticated)
			return null;

		// Check if hosting
		boolean isHosting;
		try {
			isHosting = dal.getGameServerDAO().getGameServer(account.getID()) != null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		if (isHosting) {
			// If hosting, update game server information
			try {
				dal.getGameServerDAO().updateGameServerField(account.getID(), GameServer.LAST_CHECKED, "DEFAULT");
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}

		// Update account authentication token
		AuthenticationToken newAuthenticationToken = null;
		try {
			dal.getAuthenticationTokenDAO().updateAuthenticationTokenField(account.getID(), AuthenticationToken.AUTHENTICATION_DATE, "DEFAULT");
			newAuthenticationToken = dal.getAuthenticationTokenDAO().getAuthenticationToken(account.getID());
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		// Create authentication token
		String newToken = Authenticater.getAuthenticationTokenAsHashedString(newAuthenticationToken);
		return newToken;
	}

}
