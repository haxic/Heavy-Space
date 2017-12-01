package shared;

public class DataPacket {

	byte[] data;
	int adder = 0;
	int getter = 0;

	public DataPacket(byte[] data) {
		this.data = data;
	}

	public void addByte(byte value) {
		data[adder++] = value;
	}

	public void addShort(short value) {
		data[adder++] = (byte) value;
		data[adder++] = (byte) (value >> Byte.SIZE);
	}

	public void addInteger(int value) {
		data[adder++] = (byte) value;
		data[adder++] = (byte) (value >> Byte.SIZE);
		data[adder++] = (byte) (value >> Byte.SIZE * 2);
		data[adder++] = (byte) (value >> Byte.SIZE * 3);
	}

	public void addString(String string) {
		for (int i = 0; i < string.length(); i++) {
			char value = string.charAt(i);
			data[adder++] = (byte) value;
			data[adder++] = (byte) (value >> Byte.SIZE);
		}
	}

	public void setByteAt(byte value, int position) {
		data[position] = value;
	}

	// public void shortToByteArray(short value) {
	// data[1] = (byte) (value >> Byte.SIZE);
	// data[0] = (byte) value;
	// }

	public String getStringAt(int start, int length) {
		String string = "";
		for (int i = 0; i < length; i++) {
			char value = getCharAt(start + i * 2);
			string += value;
		}
		return string;
	}
	
	public int getIntegerAt(int start) {
		int value = (data[start + 3] << (Byte.SIZE * 3));
		value |= (data[start + 2] & 0xFF) << (Byte.SIZE * 2);
		value |= (data[start + 1] & 0xFF) << (Byte.SIZE * 1);
		value |= (data[start] & 0xFF);
		return value;
	}

	public short getShortAt(int start) {
		short value = (short) ((data[start + 1] & 0xFF) << (Byte.SIZE * 1));
		value |= (data[start] & 0xFF);
		return value;
	}

	public char getCharAt(int start) {
		char value = (char) ((data[start + 1] & 0xFF) << (Byte.SIZE * 1));
		value |= (data[start] & 0xFF);
		return value;
	}

	public byte getByteAt(int start) {
		return data[start];
	}

	public String getString(int length) {
		String string = "";
		for (int i = 0; i < length; i++) {
			char value = getCharAt(getter);
			getter += 2;
			string += value;
		}
		return string;
	}

	public int getInteger() {
		int value = getIntegerAt(getter);
		getter += 4;
		return value;
	}

	public short getShort() {
		short value = getShortAt(getter);
		getter += 2;
		return value;
	}

	public char getChar() {
		char value = getCharAt(getter);
		getter += 2;
		return value;
	}

	public byte getByte() {
		byte value = getByteAt(getter);
		getter++;
		return value;
	}

	public byte[] getData() {
		return data;
	}

	public int getCurrentDataSize() {
		return adder;
	}

	public int getMaxDataSize() {
		return data.length;
	}

}
