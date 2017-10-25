package shared.dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import shared.dbo.Account;
import shared.dbo.AuthenticationToken;
import shared.dbo.GameServer;
import shared.dbo.GameServerInfo;
import shared.idal.IGameServerDAO;

public class GameServerDAO implements IGameServerDAO {
	private Connection dbc;

	public GameServerDAO(Connection dbc) {
		this.dbc = dbc;
	}

	@Override
	public List<GameServer> getGameServers() throws SQLException {
		String sql = "SELECT game_server.account_id, game_server.last_checked, authentication_token.client_ip FROM account" 
				+ " INNER JOIN game_server on account.id = game_server.account_id"
				+ " INNER JOIN authentication_token ON account.id = authentication_token.account_id;";
		
		sql = "SELECT " + GameServer.GAME_SERVER + "." + GameServer.ACCOUNT_ID + ", " + GameServer.GAME_SERVER + "." + GameServer.LAST_CHECKED + ", " + AuthenticationToken.AUTHENTICATION_TOKEN + "." + AuthenticationToken.CLIENT_IP + " FROM " + Account.ACCOUNT
				+ " INNER JOIN " + GameServer.GAME_SERVER + " ON " + Account.ACCOUNT + "." + Account.ID + " = " + GameServer.GAME_SERVER + "." + GameServer.ACCOUNT_ID
				+ " INNER JOIN "  + AuthenticationToken.AUTHENTICATION_TOKEN + " ON " + Account.ACCOUNT + "." + Account.ID + " = " + AuthenticationToken.AUTHENTICATION_TOKEN + "." + AuthenticationToken.ACCOUNT_ID + ";";
		Statement s = dbc.createStatement();
		ResultSet rs = s.executeQuery(sql);
		List<GameServer> gameServers = new ArrayList<GameServer>();
		fillGameServer(rs, gameServers);
		return gameServers;
	}

	@Override
	public List<GameServerInfo> getGameServersForClients() throws SQLException {
		String sql = "SELECT authentication_token.client_ip FROM game_server" + " INNER JOIN authentication_token ON game_server.account_id = authentication_token.account_id;";
		Statement s = dbc.createStatement();
		ResultSet rs = s.executeQuery(sql);
		List<GameServerInfo> gameServers = new ArrayList<GameServerInfo>();
		fillGameServerInfo(rs, gameServers);
		return gameServers;
	}

	@Override
	public void createGameServer(int id) throws SQLException {
		Statement s = dbc.createStatement();
		String sql = "INSERT INTO " + GameServer.GAME_SERVER + " (" + GameServer.ACCOUNT_ID + ") " + "VALUES ('" + id + "');";
		s.executeUpdate(sql);
	}

	private void fillGameServer(ResultSet rs, List<GameServer> gameServers) throws SQLException {
		while (rs.next()) {
			int accountID = rs.getInt(GameServer.ACCOUNT_ID);
			String serverIP = rs.getString(GameServer.SERVER_IP);
			Date lastChecked = rs.getDate(GameServer.LAST_CHECKED);
			gameServers.add(new GameServer(accountID, serverIP, lastChecked));
		}
	}

	private void fillGameServerInfo(ResultSet rs, List<GameServerInfo> gameServers) throws SQLException {
		while (rs.next()) {
			String serverIP = rs.getString(GameServer.SERVER_IP);
			gameServers.add(new GameServerInfo(serverIP));
		}
	}
}
