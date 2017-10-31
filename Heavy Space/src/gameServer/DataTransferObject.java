package gameServer;

import java.util.List;
import java.util.Queue;

import shared.socket.DataPacket;

public class DataTransferObject {
	Queue<byte[]> received;
	Queue<byte[]> toSend;

	public void receiveData(byte[] data) {
		received.add(data);
	}

	public void sendData(List<DataPacket> dataPackets) {
		for (DataPacket dataPacket : dataPackets)
			toSend.add(dataPacket.getData());
	}

	public void sendData(DataPacket dataPacket) {
		toSend.add(dataPacket.getData());
	}

	public boolean hasMessage() {
		return !toSend.isEmpty();
	}

	public byte[] getMessage() {
		return received.poll();
	}

}
