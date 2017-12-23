package shared.functionality;

public enum SnapshotSequenceType {
	END, CREATE, UPDATE;

	public byte asByte() {
		return (byte) this.ordinal();
	}
}
