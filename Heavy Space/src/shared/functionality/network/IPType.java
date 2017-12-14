package shared.functionality.network;

public enum IPType {
	Localhost, LAN, External;

	public IPType asHost() {
		return this.equals(Localhost) ? Localhost : LAN;
	}
}
