package gameServer.network;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import shared.socket.DataPacket;

public class DataTransferObject {
	Queue<byte[]> received = new ConcurrentLinkedQueue();
	Queue<byte[]> toSend = new ConcurrentLinkedQueue();
	private boolean disconnected;

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

	public void disconnect() {
		disconnected = true;
	}

	public boolean isDisconnected() {
		return disconnected;
	}
}
