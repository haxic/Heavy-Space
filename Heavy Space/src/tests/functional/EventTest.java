package tests.functional;

import hevent.Event;
import hevent.EventListener;
import hevent.EventManager;

public class EventTest implements EventListener {

	public static void main(String[] args) {
		EventManager eventManager = new EventManager();
		EventTest eventTest = new EventTest(eventManager);
		eventManager.createEvent(new DamageReceivedEvent(142));
		eventManager.createEvent(new AreaEnteredEvent("253"));
	}

	public EventTest(EventManager eventManager) {
		eventManager.subscribe(DamageReceivedEvent.class, this);
		eventManager.subscribe(AreaEnteredEvent.class, this);
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof DamageReceivedEvent) {
			DamageReceivedEvent damageReceivedEvent = (DamageReceivedEvent) event;
			System.out.println(damageReceivedEvent.i);
		} else if (event instanceof AreaEnteredEvent) {
			AreaEnteredEvent areaEnteredEvent = (AreaEnteredEvent) event;
			System.out.println(areaEnteredEvent.s);
		}
	}
}
