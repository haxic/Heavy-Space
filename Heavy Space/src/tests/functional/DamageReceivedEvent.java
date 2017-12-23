package tests.functional;

import hevent.Event;

public class DamageReceivedEvent extends Event {

	public int i;

	public DamageReceivedEvent(int i) {
		this.i = i;
	}

}
