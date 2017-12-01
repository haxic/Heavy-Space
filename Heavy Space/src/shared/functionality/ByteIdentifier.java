package shared.functionality;

public class ByteIdentifier {
	private byte identifier;

	public byte get() {
		byte i = identifier++;
		if (identifier > Byte.MAX_VALUE)
			identifier = Byte.MIN_VALUE;
		return i;
	}

	public byte check() {
		return identifier;
	}
}
