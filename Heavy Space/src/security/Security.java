package security;

import org.mindrot.jbcrypt.BCrypt;

public class Security {
	public static final int LOG_ROUNDS = 14;
	public static final String AUTHENTICATION_SECRET = "p5Ia2bCm45qz5DYw0dnSa";
	
	public static String encryptPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
	}
	
	public static boolean checkPassword(String password, String hash) {
		return BCrypt.checkpw(password, hash);
	}
}
