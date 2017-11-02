package gameServer.network;

import java.io.IOException;
import java.net.Socket;

public class ValidationService {
	IGameServerRequestHandler gameServerRequestHandler;

	public ValidationService(IGameServerRequestHandler gameServerRequestHandler) {
		this.gameServerRequestHandler = gameServerRequestHandler;
	}

	public void handleNewConnection(Socket socket) {
		ClientValidator clientValidator = new ClientValidator(socket);
		try {
			clientValidator.startValidation();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleException(SocketHandler socketHandler, IOException e) {
		e.printStackTrace();
		if (!socketHandler.isClosed()) {
			try {
				socketHandler.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void handleInvalidConnection(SocketHandler socketHandler, String errorMessage) {
		try {
			socketHandler.sendData(errorMessage.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleValidatedConnection(SocketHandler socketHandler, String username, String token) {
		byte[] invalidMessage = "Accepted: validation successful.".getBytes();
		try {
			socketHandler.sendData(invalidMessage);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// gameModel.addPlayer("test").getDataTransferObject()
	}

	class ClientValidator implements Runnable {
		Thread thread;
		SocketHandler socketHandler;

		public ClientValidator(Socket socket) {
			this.socketHandler = new SocketHandler(socket);
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
				if (dataReceived == null)
					handleInvalidConnection(socketHandler, "Failed to join: data is null.");
				String result = new String(dataReceived);
				try {
					String[] splitResult = result.split("\\s+");
					username = splitResult[0];
					token = splitResult[1];
				} catch (Exception e) {
					handleInvalidConnection(socketHandler, "Failed to join: couldn't parse username and token.");
				}
			} catch (IOException e) {
				handleException(socketHandler, e);
			}
			if (username == null || token == null) {
				handleInvalidConnection(socketHandler, "Failed to join: invalid credentials.");
			}
			boolean validated = gameServerRequestHandler.validateClient(username, token);
			if (validated) {
				handleValidatedConnection(socketHandler, username, token);
			} else {
				handleInvalidConnection(socketHandler, "Failed to join: client not validated.");
			}
		}

	}

}
