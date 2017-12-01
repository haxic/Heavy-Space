package tests.functional;

import shared.functionality.EventType;

public class ByteToEventType {
	public static void main(String[] args) {
		byte type = 5;
		EventType eventType = EventType.values()[type & 0xFF];
		System.out.println(eventType);
	}
}
