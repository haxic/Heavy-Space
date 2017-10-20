package shared.dal;

public interface IDataAccessLayer {

	public IAccountDAO getAccountDAO();

	public IAuthenticationTokenDAO getAuthenticationTokenDAO();

}
