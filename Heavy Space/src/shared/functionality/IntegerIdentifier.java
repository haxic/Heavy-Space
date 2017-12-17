package shared.functionality;

public class IntegerIdentifier {
	private int identifier;

	public int get() {
		int i = identifier++;
		if (identifier > Integer.MAX_VALUE)
			identifier = Integer.MIN_VALUE;
		return i;
	}

	public int check() {
		return identifier;
	}
}