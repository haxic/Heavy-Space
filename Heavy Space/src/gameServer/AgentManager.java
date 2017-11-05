package gameServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.joml.Vector3f;

import gameServer.network.SocketHandler;
import shared.DataPacket;

public class AgentManager {
	Map<String, Agent> agents;
	private long agentCounter;
	private PlayerManager playerManager;

	public AgentManager(PlayerManager playerManager) {
		this.playerManager = playerManager;
		agents = new HashMap<>();
	}

	public void handleReceivedData(Vector3f newPosition) {
		Agent agent = agents.get("hello");
		if (agent != null)
			agent.player.position = newPosition;
	}

	public void handleValidatedConnection(SocketHandler socketHandler, String username, String token) {
		Player player = playerManager.getPlayer(username);
		if (player == null)
			player = playerManager.createPlayer(username);
		agentCounter++;
		Agent agent = new Agent(player, socketHandler, username, token);
		agents.put(username, agent);
		System.out.println("Player joined: " + username);
	}

	public class Agent {
		InputHandler inputHandler;
		OutputHandler outputHandler;
		boolean shouldClose;
		SocketHandler socketHandler;
		Player player;
		String username;
		String token;

		Queue<byte[]> received = new ConcurrentLinkedQueue();
		Queue<byte[]> toSend = new ConcurrentLinkedQueue();
		private InetAddress udpAddress;

		public Agent(Player player, SocketHandler socketHandler, String username, String token) {
			this.player = player;
			this.socketHandler = socketHandler;
			this.username = username;
			this.token = token;
			udpAddress = socketHandler.getAddress();
			outputHandler = new OutputHandler();
			outputHandler.start();
			inputHandler = new InputHandler();
			inputHandler.start();
		}

		// Send several data packets
		public void sendData(List<DataPacket> dataPackets) {
			for (DataPacket dataPacket : dataPackets)
				toSend.add(dataPacket.getData());
		}

		// Send one data packet
		public void sendData(DataPacket dataPacket) {
			toSend.add(dataPacket.getData());
		}

		// Get received data
		public byte[] getData() {
			return received.poll();
		}

		public void requestClose() {
			shouldClose = true;
		}

		public InetAddress getUDPAddress() {
			return udpAddress;
		}

		private void disconnect() {
			requestClose();
			try {
				if (!socketHandler.isClosed())
					socketHandler.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		class InputHandler extends Thread implements Runnable {
			@Override
			public void run() {
				while (!shouldClose && socketHandler.isClosed()) {
					try {
						byte[] data = socketHandler.readData();
						if (data != null) {
							received.add(data);
							System.out.println("Data " + data + " received from " + username);
						}
					} catch (IOException e) {
						e.printStackTrace();
						shouldClose = true;
					}
				}
			}
		}

		class OutputHandler extends Thread implements Runnable {
			@Override
			public void run() {
				while (!shouldClose) {
					while (!toSend.isEmpty() && !shouldClose && !socketHandler.isClosed()) {
						byte[] data = toSend.poll();
						if (data == null || data.length == 0)
							continue;
						try {
							socketHandler.sendData(data);
							System.out.println("Data " + data + " send to " + username);
						} catch (IOException e) {
							e.printStackTrace();
							shouldClose = true;
						}
					}
					try {
						sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				disconnect();
			}
		}
	}
}
