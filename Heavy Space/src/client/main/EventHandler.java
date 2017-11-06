package client.main;

import java.util.LinkedList;
import java.util.Queue;

public class EventHandler {
	Queue<Event> events = new LinkedList<>();

	public Event poll() {
		return events.poll();
	}

	public void addEvent(Event event) {
		events.add(event);
	}

}
