package shared.functionality;

public class ShortIdentifier {
	private short identifier;

	public short get() {
		short i = identifier++;
		if (identifier > Short.MAX_VALUE)
			identifier = Short.MIN_VALUE;
		return i;
	}

	public short check() {
		return identifier;
	}
}
