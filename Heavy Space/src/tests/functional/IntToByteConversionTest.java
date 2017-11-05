package tests.functional;

import org.joml.Vector3f;

import shared.DataPacket;

public class IntToByteConversionTest {
	public static void main(String[] args) {
		DataPacket dataPacket = new DataPacket(new byte[508]);

		System.out.println((byte) 12);

		byte messageType = 0; // 0
		byte bulkSize = 1; // 1
		float positionX = -2045.241f; // 2
		float positionY = 348.161f; // 6
		float positionZ = 1238.001f; // 10
		float orientationX = 0.09f; // 14
		float orientationY = 1.01f; // 16
		float orientationZ = 0.57f; // 18

		Vector3f position = new Vector3f(positionX, positionY, positionZ);
		Vector3f orientation = new Vector3f(orientationX, orientationY, orientationZ);

		// Add message type
		dataPacket.addByte(messageType);
		// Add bulk
		dataPacket.addByte(bulkSize);
		// Add position
		dataPacket.addInteger((int) (position.x * 1000));
		dataPacket.addInteger((int) (position.y * 1000));
		dataPacket.addInteger((int) (position.z * 1000));
		// Add orientation
		dataPacket.addShort((short) (orientation.x * 100));
		dataPacket.addShort((short) (orientation.y * 100));
		dataPacket.addShort((short) (orientation.z * 100));
		System.out.println(dataPacket.getByte());
		System.out.println(dataPacket.getByte());
		Vector3f newPosition = new Vector3f(dataPacket.getInteger() / 1000.0f, dataPacket.getInteger() / 1000.0f, dataPacket.getInteger() / 1000.0f);
		System.out.println(newPosition.x + " " + newPosition.y + " " + newPosition.z);
		System.out.println(new Vector3f(dataPacket.getShort() / 100.0f, dataPacket.getShort() / 100.0f, dataPacket.getShort() / 100.0f));
		// Get message type
		System.out.println(dataPacket.getByteAt(0));
		// Get bulk size
		System.out.println(dataPacket.getByteAt(1));
		// Get position
		System.out.println(new Vector3f(dataPacket.getIntegerAt(2) / 1000.0f, dataPacket.getIntegerAt(6) / 1000.0f, dataPacket.getIntegerAt(10) / 1000.0f));
		// Get orientation
		System.out.println(new Vector3f(dataPacket.getShortAt(14) / 100.0f, dataPacket.getShortAt(16) / 100.0f, dataPacket.getShortAt(18) / 100.0f));

	}

}
