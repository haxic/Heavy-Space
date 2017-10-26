package shared.dal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
	public GameServer getGameServer(int accountID) throws SQLException {
		Statement s = dbc.createStatement();
		ResultSet rs = s.executeQuery("SELECT * FROM " + GameServer.GAME_SERVER + " WHERE " + GameServer.ACCOUNT_ID + " = '" + accountID + "';");
		if (rs.next())
			return fillGameServer(rs);
		return null;
	}

	@Override
	public List<GameServer> getGameServers() throws SQLException {
		String sql = "SELECT " + GameServer.GAME_SERVER + "." + GameServer.ACCOUNT_ID + ", " + GameServer.GAME_SERVER + "." + GameServer.LAST_CHECKED + ", " + AuthenticationToken.AUTHENTICATION_TOKEN
				+ "." + AuthenticationToken.CLIENT_IP + " FROM " + Account.ACCOUNT + " INNER JOIN " + GameServer.GAME_SERVER + " ON " + Account.ACCOUNT + "." + Account.ID + " = "
				+ GameServer.GAME_SERVER + "." + GameServer.ACCOUNT_ID + " INNER JOIN " + AuthenticationToken.AUTHENTICATION_TOKEN + " ON " + Account.ACCOUNT + "." + Account.ID + " = "
				+ AuthenticationToken.AUTHENTICATION_TOKEN + "." + AuthenticationToken.ACCOUNT_ID + ";";
		Statement s = dbc.createStatement();
		ResultSet rs = s.executeQuery(sql);
		List<GameServer> gameServers = new ArrayList<GameServer>();
		while (rs.next()) {
			gameServers.add(fillGameServerFullData(rs));
		}
		return gameServers;
	}

	@Override
	public List<GameServerInfo> getGameServersForClients() throws SQLException {
		String sql = "SELECT " + AuthenticationToken.AUTHENTICATION_TOKEN + "." + AuthenticationToken.CLIENT_IP + " FROM game_server" + " INNER JOIN " + AuthenticationToken.AUTHENTICATION_TOKEN
				+ " ON " + GameServer.GAME_SERVER + "." + GameServer.ACCOUNT_ID + " = " + AuthenticationToken.AUTHENTICATION_TOKEN + "." + AuthenticationToken.ACCOUNT_ID + ";";
		Statement s = dbc.createStatement();
		ResultSet rs = s.executeQuery(sql);
		List<GameServerInfo> gameServers = new ArrayList<GameServerInfo>();
		while (rs.next()) {
			gameServers.add(fillGameServerInfo(rs));
		}
		return gameServers;
	}

	@Override
	public void createGameServer(int accountID) throws SQLException {
		Statement s = dbc.createStatement();
		String sql = "INSERT INTO " + GameServer.GAME_SERVER + " (" + GameServer.ACCOUNT_ID + ") " + "VALUES ('" + accountID + "');";
		s.executeUpdate(sql);
	}

	@Override
	public void removeTimedOutGameServers() throws SQLException {
		Statement s = dbc.createStatement();
		String sql = "DELETE FROM " + GameServer.GAME_SERVER + " WHERE " + GameServer.LAST_CHECKED + " < (NOW() - INTERVAL '1 minute');";
		s.executeUpdate(sql);
	}

	private GameServer fillGameServer(ResultSet rs) throws SQLException {
		int accountID = rs.getInt(GameServer.ACCOUNT_ID);
		Timestamp lastChecked = rs.getTimestamp(GameServer.LAST_CHECKED);
		return new GameServer(accountID, null, lastChecked != null ? lastChecked.toLocalDateTime() : null);
	}

	private GameServer fillGameServerFullData(ResultSet rs) throws SQLException {
		int accountID = rs.getInt(GameServer.ACCOUNT_ID);
		String serverIP = rs.getString(GameServer.SERVER_IP);
		Timestamp lastChecked = rs.getTimestamp(GameServer.LAST_CHECKED);
		return new GameServer(accountID, serverIP, lastChecked != null ? lastChecked.toLocalDateTime() : null);
	}

	private GameServerInfo fillGameServerInfo(ResultSet rs) throws SQLException {
		String serverIP = rs.getString(GameServer.SERVER_IP);
		return new GameServerInfo(serverIP);
	}

	@Override
	public void updateGameServerField(int accountID, String field, Object value) throws SQLException {
		if (value instanceof String)
			if (!value.equals("DEFAULT"))
				value = "'" + value + "'";
		Statement s = dbc.createStatement();
		s.executeUpdate("UPDATE " + GameServer.GAME_SERVER + " SET " + field + " = " + value + " WHERE " + GameServer.ACCOUNT_ID + " = " + accountID + ";");
	}

}
