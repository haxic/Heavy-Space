package shared.idal;

public interface IDataAccessLayer {

	public IAccountDAO getAccountDAO();

	public IAuthenticationTokenDAO getAuthenticationTokenDAO();

	public IGameServerDAO getGameServerDAO();

}
