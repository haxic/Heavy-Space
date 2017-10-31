package shared.socket;

public class DataPacket {

	byte[] data = new byte[504];
	int position = 0;

	public void addByte(byte value) {
		data[position++] = value;
	}

	public void addShort(short value) {
		data[position++] = (byte) value;
		data[position++] = (byte) (value >> Byte.SIZE);
	}

	public void addInteger(int value) {
		data[position++] = (byte) value;
		data[position++] = (byte) (value >> Byte.SIZE);
		data[position++] = (byte) (value >> Byte.SIZE * 2);
		data[position++] = (byte) (value >> Byte.SIZE * 3);
	}

	public void shortToByteArray(short value) {
		data[1] = (byte) (value >> Byte.SIZE);
		data[0] = (byte) value;
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

	public byte getByteAt(int start) {
		return data[start];
	}

	public byte[] getData() {
		return data;
	}

}
