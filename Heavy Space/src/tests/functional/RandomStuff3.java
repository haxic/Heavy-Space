package tests.functional;

import org.joml.Vector3f;

public class RandomStuff3 {

	public static void main(String[] args) {
		byte newGameState = Byte.MAX_VALUE;
		byte latest = Byte.MIN_VALUE+100;
		int last = 0;

		if (newGameState > latest && !(newGameState > Byte.MAX_VALUE / 2 && latest < Byte.MIN_VALUE / 2)
				|| (newGameState < Byte.MIN_VALUE / 2 && latest > Byte.MAX_VALUE / 2)) {
			if (++last >= 4)
				last = 0;
			latest = newGameState;
		}
		System.out.println(latest);
	}

}
