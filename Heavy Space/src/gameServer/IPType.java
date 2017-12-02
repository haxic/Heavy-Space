package gameServer;

public enum IPType {
	Localhost, LAN, External;

	public IPType asHost() {
		return this.equals(Localhost) ? Localhost : LAN;
	}
}
