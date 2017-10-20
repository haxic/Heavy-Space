package tests;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.mindrot.jbcrypt.BCrypt;

public class TestPasswordSaltHash {
	public static void main(String[] args) {
		String password = "salt";
		SecureRandom random = new SecureRandom();
		random.setSeed(new byte[128]);
		String salt = BCrypt.gensalt(13, random);
		String hashedPassword = BCrypt.hashpw(password, salt);
		System.out.println(BCrypt.gensalt(13, random));
		System.out.println(BCrypt.gensalt(13, random));
		System.out.println(BCrypt.gensalt(13, random));
		System.out.println(BCrypt.gensalt(13, random));

		try {
			random = new SecureRandom().getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			random = new SecureRandom();
		}
		byte[] values = new byte[64];
		random.nextBytes(values);
//		System.out.println(new String(values));
	}
}
