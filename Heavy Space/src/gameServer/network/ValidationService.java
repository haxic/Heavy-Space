package gameServer.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ValidationService {
	IServerCommunicator serverCommuicator;
	int timeout;
	Set<ValidationTask> validationTasks = Collections.newSetFromMap(new ConcurrentHashMap<ValidationTask, Boolean>());
	int validationCounter;

	public ValidationService(IServerCommunicator serverCommuicator, int timeout) {
		this.serverCommuicator = serverCommuicator;
		this.timeout = timeout;
	}

	public void handleNewConnection(Socket socket) {
		ValidationTask validationTask = new ValidationTask(socket);
		validationCounter++;
		validationTasks.add(validationTask);
		try {
			validationTask.startValidation();
		} catch (IOException e) {
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
		byte[] invalidMessage = "Accepted: validation successful.".getBytes();
		try {
			validationTask.socketHandler.sendData(invalidMessage);
		} catch (IOException e) {
			// e.printStackTrace();
		}
		validationTasks.remove(validationTask);
		// gameModel.addPlayer("test").getDataTransferObject()
		
	}

	class ValidationTask implements Runnable {
		Thread thread;
		SocketHandler socketHandler;

		public ValidationTask(Socket socket) {
			this.socketHandler = new SocketHandler(socket);
		}

		public void startValidation() throws IOException {
			thread = new Thread(this);
			thread.start();
		}

		@Override
		public void run() {
			try {
				socketHandler.setSoTimeout(timeout);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			String username = null;
			String token = null;
			try {
				byte[] dataReceived = socketHandler.readData();
				if (dataReceived == null) {
					handleInvalidConnection(this, "Failed to join: data is null.");
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
			boolean validated = serverCommuicator.validateClient(token, username);
			if (validated) {
				handleValidatedConnection(this, username, token);
				return;
			} else {
				handleInvalidConnection(this, "Failed to join: client not validated.");
				return;
			}
		}

	}

}
