package client.main;

public class Event {
	public static final int JOIN_SERVER = 0;
	public static final int DISCONNECT = 1;
	public static final int AUTHENTICATE = 2;
	public static final int JOIN_SERVER_FAILED = 3;

	public int type;
	public Object[] data;

	public Event(int type, Object... data) {
		System.out.println("Event created: " + type);
		this.type = type;
		this.data = data;
	}

}
