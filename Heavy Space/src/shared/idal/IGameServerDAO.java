package shared.idal;

import java.sql.SQLException;
import java.util.List;

import shared.dbo.GameServer;
import shared.dbo.GameServerInfo;

public interface IGameServerDAO {
	public List<GameServer> getGameServers() throws SQLException;
	public List<GameServerInfo> getGameServersForClients() throws SQLException;
	public void createGameServer(int acountID) throws SQLException;
	public GameServer getGameServer(int acountID) throws SQLException;
	public void updateGameServerField(int id, String field, Object value) throws SQLException;
	public void removeTimedOutGameServers() throws SQLException;
}
