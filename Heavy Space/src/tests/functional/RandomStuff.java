package tests.functional;

public class RandomStuff {

	public static void main(String[] args) {
		boolean[] boolsfirst = new boolean[] { true, true, false, false, true, false, false, true };
		for (int i = 0; i < boolsfirst.length; i++) {
			System.out.print(boolsfirst[i] + " ");
		}
		System.out.println();

		// byte b = Byte.parseByte("00000111", 2);
		byte byteFirst = byteFromBooleanArray(boolsfirst);

		// b = (byte) (b | (1 << 4));
		System.out.println(String.format("%8s", Integer.toBinaryString(byteFirst & 0xFF)).replace(' ', '0'));

		boolean[] boolsSecond = booleanArrayFromByte(byteFirst);

		for (int i = 0; i < boolsSecond.length; i++) {
			System.out.print(boolsSecond[i] + " ");
		}
		System.out.println();
		byte byteSecond = byteFromBooleanArray(boolsSecond);
		System.out.println(String.format("%8s", Integer.toBinaryString(byteSecond & 0xFF)).replace(' ', '0'));
		System.out.println("Bit at 0,1,2,3: " + bitAt(byteSecond, 0) + "," + bitAt(byteSecond, 1) + "," + bitAt(byteSecond, 2) + "," + bitAt(byteSecond, 3));
		System.out.println("BitAsBoolean at 0,1,2,3: " + bitAtAsBoolean(byteSecond, 0) + "," + bitAtAsBoolean(byteSecond, 1) + "," + bitAtAsBoolean(byteSecond, 2) + "," + bitAtAsBoolean(byteSecond, 3));
	}

	public static byte byteFromBooleanArray(boolean[] bools) {
		byte b = 0;
		for (int i = 7; i >= 0; i--)
			if (bools[i])
				b = (byte) (b | (1 << i));
		return b;
	}

	private static boolean[] booleanArrayFromByte(byte b) {
		boolean[] booleans = new boolean[8];
		for (int i = 0; i < booleans.length; i++)
			booleans[i] = bitAtAsBoolean(b, i);
		return booleans;
	}

	private static boolean bitAtAsBoolean(byte b, int i) {
		return bitAt(b, i) == 0 ? false : true;
	}

	private static int bitAt(byte b, int i) {
		return (b >> i) & 1;
	}
}
