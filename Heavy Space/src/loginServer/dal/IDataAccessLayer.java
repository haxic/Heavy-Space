package loginServer.dal;

public interface IDataAccessLayer {

	public IAccountDAO getAccountDAO();

	public IAuthenticationTokenDAO getAuthenticationTokenDAO();

}
