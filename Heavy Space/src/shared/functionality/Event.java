package shared.functionality;

public class Event {

	public EventType type;
	public Object[] data;

	public Event(EventType type, Object... data) {
		this.type = type;
		this.data = data;
	}

}
