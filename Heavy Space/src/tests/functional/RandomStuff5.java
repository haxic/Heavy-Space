package tests.functional;

public class RandomStuff5 {

	public static void main(String[] args) {
		short nextSnapshot = (short) ((1000 % Short.MAX_VALUE) / 3);
		
		System.out.println(nextSnapshot);
	}

}
