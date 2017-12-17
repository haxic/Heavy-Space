package tests.functional;

import shared.functionality.Globals;

public class RandomStuff5 {

	public static void main(String[] args) {
		Globals.tick = (short) (10940 % Short.MAX_VALUE);
		System.out.println(Globals.tick);
	}

}
