package tests.functional;

import static org.junit.Assert.assertEquals;

import org.joml.Vector3f;
import org.junit.Test;

import shared.functionality.DataPacket;

public class DataPacketTest {
	@Test
	public void testDataPacket() {
		DataPacket dataPacket = new DataPacket(new byte[508]);

		byte messageType = 0; // 0
		byte bulkSize = 1; // 1
		float positionX = -2045.241f; // 2
		float positionY = 348.161f; // 6
		float positionZ = 1238.001f; // 10
		float orientationX = 0.09f; // 14
		float orientationY = 1.01f; // 16
		float orientationZ = 0.57f; // 18
		String text = "Testing testing 1234+1?!#00,._";
		byte finish = 100; // 1
		Vector3f position = new Vector3f(positionX, positionY, positionZ);
		Vector3f orientation = new Vector3f(orientationX, orientationY, orientationZ);

		// Check data packet size before adding anything
		assertEquals(0, dataPacket.size());

		// Add message type
		dataPacket.addByte(messageType);
		// Add bulk
		dataPacket.addByte(bulkSize);
		// Add position
		dataPacket.addInteger((int) (position.x * 1000));
		dataPacket.addInteger((int) (position.y * 1000));
		dataPacket.addFloat(position.z);
		// Add orientation
		dataPacket.addShort((short) (orientation.x * 100));
		dataPacket.addShort((short) (orientation.y * 100));
		dataPacket.addShort((short) (orientation.z * 100));
		// Add some text
		dataPacket.addString(text);
		// Add a finish value
		dataPacket.addByte(finish);

		// Check data packet size after filling it
		assertEquals(81, dataPacket.size());

		// System.out.println("--------------------------------------");
		// ----------- Serial data extraction -----------
		byte type1 = dataPacket.getByte();
		byte size1 = dataPacket.getByte();
		float posX1 = dataPacket.getInteger() / 1000.0f;
		float posY1 = dataPacket.getInteger() / 1000.0f;
		float posZ1 = dataPacket.getFloat();
		float oriX1 = dataPacket.getShort() / 100.0f;
		float oriY1 = dataPacket.getShort() / 100.0f;
		float oriZ1 = dataPacket.getShort() / 100.0f;
		String text1 = dataPacket.getString(text.length());
		byte finish1 = dataPacket.getByte();

		// Check that all serially extracted data matches original data
		assertEquals(messageType, type1);
		assertEquals(bulkSize, size1);
		assertEquals(positionX, posX1, 0);
		assertEquals(positionY, posY1, 0);
		assertEquals(positionZ, posZ1, 0);
		assertEquals(orientationX, oriX1, 0);
		assertEquals(orientationY, oriY1, 0);
		assertEquals(orientationZ, oriZ1, 0);
		assertEquals(text, text1);
		assertEquals(finish, finish1);

		// Vector3f position1 = new Vector3f(posX1, posY1, posZ1);
		// Vector3f orientation1 = new Vector3f(oriX1, oriY1, oriZ1);
		// System.out.println(type1);
		// System.out.println(size1);
		// System.out.println(position1.x + " " + position1.y + " " +
		// position1.z);
		// System.out.println(orientation1.x + " " + orientation1.y + " " +
		// orientation1.z);
		// System.out.println(text1);
		// System.out.println(finish1);

		// System.out.println("--------------------------------------");
		// ----------- Indexed data extraction -----------
		byte type2 = dataPacket.getByteAt(0);
		byte size2 = dataPacket.getByteAt(1);
		float posX2 = dataPacket.getIntegerAt(2) / 1000.0f;
		float posY2 = dataPacket.getIntegerAt(6) / 1000.0f;
		float posZ2 = dataPacket.getFloatAt(10);
		float oriX2 = dataPacket.getShortAt(14) / 100.0f;
		float oriY2 = dataPacket.getShortAt(16) / 100.0f;
		float oriZ2 = dataPacket.getShortAt(18) / 100.0f;
		String text2 = dataPacket.getStringAt(20, text.length());
		byte finish2 = dataPacket.getByteAt(80);

		assertEquals(messageType, type2);
		assertEquals(bulkSize, size2);
		assertEquals(positionX, posX2, 0);
		assertEquals(positionY, posY2, 0);
		assertEquals(positionZ, posZ2, 0);
		assertEquals(orientationX, oriX1, 0);
		assertEquals(orientationY, oriY2, 0);
		assertEquals(orientationZ, oriZ2, 0);
		assertEquals(text, text2);
		assertEquals(finish, finish2);

		// Vector3f position2 = new Vector3f(posX2, posY2, posZ2);
		// Vector3f orientation2 = new Vector3f(oriX2, oriY2, oriZ2);
		// System.out.println(type2);
		// System.out.println(size2);
		// System.out.println(position2.x + " " + position2.y + " " +
		// position2.z);
		// System.out.println(orientation2.x + " " + orientation2.y + " " +
		// orientation2.z);
		// System.out.println(text2);
		// System.out.println(finish2);
	}

}
