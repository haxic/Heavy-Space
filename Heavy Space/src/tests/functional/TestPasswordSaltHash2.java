package tests.functional;

import java.util.UUID;

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
		System.out.println(hashpw.substring(7).getBytes().length);

		String uuid = UUID.randomUUID().toString().replace("-", "");
		System.out.println(uuid);
		System.out.println(uuid.length());
		System.out.println();
		byte b = Byte.parseByte("00000111", 2);
		for (int i = 0; i < 8; i++) {
			System.out.println((b >> i) & 1);
		}
	}
}
