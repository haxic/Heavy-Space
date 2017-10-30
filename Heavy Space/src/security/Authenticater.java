package security;

import org.mindrot.jbcrypt.BCrypt;

import shared.dbo.AuthenticationToken;

public class Authenticater {
	private static final int LOG_ROUNDS = 14;
	private static final String AUTHENTICATION_SECRET = "p5Ia2bCm45qz5DYw0dnSa";
	private static final String PASSWORD_SECRET = "0Tmn1lKt61Bkja6t38oPLdfgHv";

	public static String encryptPassword(String password) {
		return BCrypt.hashpw(password + PASSWORD_SECRET, BCrypt.gensalt(LOG_ROUNDS));
	}

	public static boolean checkPassword(String password, String hash) {
		return BCrypt.checkpw(password + PASSWORD_SECRET, hash);
	}

	public static String getAuthenticationTokenAsHashedString(AuthenticationToken authenticationToken) {
		return BCrypt.hashpw(buildAuthenticationTokenString(authenticationToken), BCrypt.gensalt(LOG_ROUNDS));
	}

	public static boolean checkAuthenticationToken(AuthenticationToken authenticationToken, String hashedToken) {
		return BCrypt.checkpw(buildAuthenticationTokenString(authenticationToken), hashedToken);
	}

	private static String buildAuthenticationTokenString(AuthenticationToken authenticationToken) {
		return authenticationToken.getAccountID()
				+ "//" + authenticationToken.getClientIP()
				+ "//" + authenticationToken.getMasterServerIP()
				+ "//" + authenticationToken.getAuthenticationDate()
				+ "//" + AUTHENTICATION_SECRET;
	}
}
