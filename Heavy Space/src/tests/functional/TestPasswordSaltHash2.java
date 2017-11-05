package tests.functional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.mindrot.jbcrypt.BCrypt;

public class TestPasswordSaltHash2 {
	public static void main(String[] args) {
		System.out.println(BCrypt.hashpw("test", BCrypt.gensalt()));
		System.out.println(BCrypt.hashpw("asdgsadgerg", BCrypt.gensalt()));
		System.out.println(BCrypt.hashpw("mad drunk yeah", BCrypt.gensalt()));
		System.out.println(BCrypt.hashpw("xDDDDDDDDDDDDD", BCrypt.gensalt()));
		String hashpw = BCrypt.hashpw("1234 nono", BCrypt.gensalt());
		System.out.println(hashpw);
		System.out.println(hashpw.substring(7));
		System.out.println(hashpw.getBytes().length);
	}
}
