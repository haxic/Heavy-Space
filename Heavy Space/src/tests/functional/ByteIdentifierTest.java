package tests.functional;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import shared.functionality.ByteIdentifier;

public class ByteIdentifierTest {
	@Test
	public void testDataPacket() {
		ByteIdentifier identifier = new ByteIdentifier();
		for (int i = 0; i <= 127; i++) {
			assertEquals(i, identifier.get());
		}
		for (int i = -128; i <= 127; i++) {
			assertEquals(i, identifier.get());
		}
	}

}
