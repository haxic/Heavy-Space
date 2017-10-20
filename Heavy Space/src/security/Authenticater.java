package security;

import org.mindrot.jbcrypt.BCrypt;

import shared.dbo.AuthenticationToken;

public class Authenticater {
	private static final int LOG_ROUNDS = 14;
	private static final String AUTHENTICATION_SECRET = "p5Ia2bCm45qz5DYw0dnSa";

	public static String encryptPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
	}

	public static boolean checkPassword(String password, String hash) {
		return BCrypt.checkpw(password, hash);
	}

	public static String getAuthenticationTokenAsHashedString(AuthenticationToken authenticationToken) {
		return encryptPassword(buildAuthenticationTokenString(authenticationToken));
	}

	public static boolean checkAuthenticationToken(AuthenticationToken authenticationToken, String hashedToken) {
		return checkPassword(buildAuthenticationTokenString(authenticationToken), hashedToken);
	}

	private static String buildAuthenticationTokenString(AuthenticationToken authenticationToken) {
		return authenticationToken.getAccountID()
				+ "//" + authenticationToken.getClientIP()
				+ "//" + authenticationToken.getMasterServerIP()
				+ "//" + authenticationToken.getAuthenticationDate()
				+ "//" + AUTHENTICATION_SECRET;
	}
}
