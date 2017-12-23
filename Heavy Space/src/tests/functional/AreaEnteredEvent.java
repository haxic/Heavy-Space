package tests.functional;

import hevent.Event;

public class AreaEnteredEvent extends Event {
	public String s;

	public AreaEnteredEvent(String s) {
		this.s = s;
	}

}
