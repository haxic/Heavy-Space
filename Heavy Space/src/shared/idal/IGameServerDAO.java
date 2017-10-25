package shared.idal;

import java.sql.SQLException;
import java.util.List;

import shared.dbo.GameServer;
import shared.dbo.GameServerInfo;

public interface IGameServerDAO {
	public List<GameServer> getGameServers() throws SQLException;
	public List<GameServerInfo> getGameServersForClients() throws SQLException;
	public void createGameServer(int id) throws SQLException;
}
