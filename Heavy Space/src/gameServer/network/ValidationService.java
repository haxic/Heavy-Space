package gameServer.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import gameServer.ClientManager;
import shared.functionality.TCPSocket;

public class ValidationService {
	private IServerCommunicator serverCommuicator;
	private int timeout;
	private Set<ValidationTask> validationTasks = Collections.newSetFromMap(new ConcurrentHashMap<ValidationTask, Boolean>());
	private int validationCounter;
	private ClientManager clientManager;
	private boolean local;

	public ValidationService(IServerCommunicator serverCommuicator, ClientManager agentManager, int timeout, boolean local) {
		this.serverCommuicator = serverCommuicator;
		this.clientManager = agentManager;
		this.timeout = timeout;
		this.local = local;
	}

	public void handleNewConnection(Socket socket) {
		ValidationTask validationTask = new ValidationTask(socket);
		validationCounter++;
		validationTasks.add(validationTask);
		try {
			validationTask.startValidation();
		} catch (IOException e) {
			if (!validationTask.socketHandler.isClosed()) {
				try {
					validationTask.socketHandler.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}

	public int getNumberOfCurrentlyActivevalidationTasks() {
		return validationTasks.size();
	}

	public int getValidationCounterCurrentValue() {
		return validationCounter;
	}

	private void handleException(ValidationTask validationTask, IOException e) {
		// e.printStackTrace();
		if (!validationTask.socketHandler.isClosed()) {
			try {
				validationTask.socketHandler.close();
			} catch (IOException e1) {
				// e1.printStackTrace();
			}
		}
		validationTasks.remove(validationTask);
	}

	private void handleInvalidConnection(ValidationTask validationTask, String errorMessage) {
		try {
			validationTask.socketHandler.sendData(errorMessage.getBytes());
		} catch (IOException e) {
			// e.printStackTrace();
		}
		if (!validationTask.socketHandler.isClosed()) {
			try {
				validationTask.socketHandler.close();
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		validationTasks.remove(validationTask);

	}

	private void handleValidatedConnection(ValidationTask validationTask, String username, String token) {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		byte[] uuidResponse = ("Accepted:" + uuid).getBytes();
		try {
			validationTask.socketHandler.sendData(uuidResponse);
		} catch (IOException e) {
			handleException(validationTask, e);
			return;
		}
		validationTasks.remove(validationTask);
		clientManager.handleValidatedTCPConnection(validationTask.socketHandler, uuid, username, token);
	}

	class ValidationTask implements Runnable {
		Thread thread;
		TCPSocket socketHandler;

		public ValidationTask(Socket socket) {
			this.socketHandler = new TCPSocket(socket, timeout);
		}

		public void startValidation() throws IOException {
			thread = new Thread(this);
			thread.start();
		}

		@Override
		public void run() {
			String username = null;
			String token = null;
			try {
				byte[] dataReceived = socketHandler.readData();
				if (dataReceived == null) {
					handleInvalidConnection(this, "Failed to join: failed to receive data.");
					return;
				}
				String result = new String(dataReceived);
				try {
					String[] splitResult = result.split("\\s+");
					username = splitResult[0];
					token = splitResult[1];
				} catch (Exception e) {
					handleInvalidConnection(this, "Failed to join: couldn't parse username and token.");
					return;
				}
			} catch (IOException e) {
				handleException(this, e);
				return;
			}
			if (username == null || token == null) {
				handleInvalidConnection(this, "Failed to join: invalid credentials.");
			}
			if (local || serverCommuicator.validateClient(token, username)) {
				handleValidatedConnection(this, username, token);
				return;
			} else {
				handleInvalidConnection(this, "Failed to join: client not validated.");
				return;
			}
		}

	}

}
