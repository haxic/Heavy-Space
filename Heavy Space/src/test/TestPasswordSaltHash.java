package test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TestPasswordSaltHash {
	public static void main(String[] args) {
		SecureRandom random;
		try {
			random = new SecureRandom().getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			random = new SecureRandom();
		}
		byte[] values = new byte[64];
		random.nextBytes(values);
		System.out.println(new String(values));
	}
}
