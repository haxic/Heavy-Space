package shared.functionality;

public class Event {

	public EventType type;
	public Object[] data;

	public Event(EventType type, Object... data) {
		System.out.println("Event created: " + type);
		this.type = type;
		this.data = data;
	}

}
