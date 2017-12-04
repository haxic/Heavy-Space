package utilities;

public class BitConverter {
	public static byte byteFromBooleanArray(boolean[] bools) {
		byte b = 0;
		for (int i = 7; i >= 0; i--)
			if (bools[i])
				b = (byte) (b | (1 << i));
		return b;
	}

	public static boolean[] booleanArrayFromByte(byte b) {
		boolean[] booleans = new boolean[8];
		for (int i = 0; i < booleans.length; i++)
			booleans[i] = bitAtAsBoolean(b, i);
		return booleans;
	}

	public static boolean bitAtAsBoolean(byte b, int i) {
		return bitAt(b, i) == 0 ? false : true;
	}

	public static int bitAt(byte b, int i) {
		return (b >> i) & 1;
	}
}
