package hevent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
	public static int idCounter;
	private Map<Class<? extends Event>, List<EventListener>> subscribers = new HashMap<Class<? extends Event>, List<EventListener>>();

	public void subscribe(Class<? extends Event> eventClass, EventListener eventListener) {
		List<EventListener> eventListeners = subscribers.get(eventClass);
		if (eventListeners == null) {
			eventListeners = new ArrayList<EventListener>();
			subscribers.put(eventClass, eventListeners);
		}
		eventListeners.add(eventListener);
	}

	public void unsubscribe(Class<? extends Event> eventClass, EventListener eventListener) {
		List<EventListener> eventListeners = subscribers.get(eventClass);
		if (eventListeners == null)
			return;
		eventListeners.remove(eventListener);
	}

	public void createEvent(Event event) {
		List<EventListener> eventListeners = subscribers.get(event.getClass());
		if (eventListeners == null)
			return;
		for (EventListener eventListener : eventListeners) {
			eventListener.handleEvent(event);
		}
	}
}
