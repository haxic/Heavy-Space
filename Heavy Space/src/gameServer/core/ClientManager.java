package gameServer.core;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gameServer.components.ClientComponent;
import gameServer.components.ClientGameDataTransferComponent;
import gameServer.components.ClientPendingComponent;
import gameServer.components.ClientValidatedComponent;
import hecs.Entity;
import hecs.EntityContainer;
import hecs.EntityManager;
import shared.components.ObjectComponent;
import shared.functionality.DataPacket;
import shared.functionality.Globals;
import shared.functionality.network.RequestType;
import shared.functionality.network.TCPSocket;

public class ClientManager implements EntityContainer {
	private Map<String, Entity> clients;
	private Map<Entity, String> uuids;
	private EntityManager entityManager;
	private PlayerManager playerManager;

	public ClientManager(EntityManager entityManager, PlayerManager playerManager) {
		this.entityManager = entityManager;
		this.playerManager = playerManager;
		clients = new HashMap<>();
		uuids = new HashMap<>();
	}

	public void handleValidatedTCPConnection(TCPSocket socketHandler, String uuid, String username, String token) {
		Entity client = entityManager.createEntity();
		ClientComponent clientComponent = new ClientComponent(socketHandler, uuid, username, token);
		ClientPendingComponent pendingValidationComponent = new ClientPendingComponent();
		entityManager.addComponent(clientComponent, client);
		entityManager.addComponent(pendingValidationComponent, client);
		clients.put(uuid, client);
		uuids.put(client, uuid);
		client.attach(this);
		clientComponent.start();
	}

	public boolean udpAuthenticationRequest(String uuid, InetAddress address, int port) {
		Entity client = clients.get(uuid);

		// Return false if the uuid doesn't exist
		if (client == null)
			return false;

		// Return true if no longer pending or if the pending is validated
		ClientPendingComponent pendingValidationComponent = (ClientPendingComponent) entityManager.getComponentInEntity(client, ClientPendingComponent.class);
		if (pendingValidationComponent == null || pendingValidationComponent.isValidated())
			return true;

		// Remove client as pending
		// entityManager.removeComponentAll(ClientPendingComponent.class, client);
		pendingValidationComponent.validate();

		ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(client, ClientComponent.class);

		// Add existing or create new player for client
		Entity player = playerManager.getPlayer(clientComponent.getUsername());
		if (player == null)
			player = playerManager.createPlayer(clientComponent.getUsername());
		// Add client as active
		clients.put(uuid, client);
		clientComponent.setPlayer(player);
		clientComponent.setUDPInetAddress(address);
		clientComponent.setUDPPort(port);
		return true;
	}

	public Map<String, Entity> getClients() {
		return clients;
	}

	public Entity getClient(String uuid) {
		return clients.get(uuid);
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(ClientComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		List<Entity> removed = new ArrayList<>();
		for (Entity entity : entities) {
			ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(entity, ClientComponent.class);
			ClientValidatedComponent clientValidatedComponent = (ClientValidatedComponent) entityManager.getComponentInEntity(entity, ClientValidatedComponent.class);
			ClientPendingComponent pendingValidationComponent = (ClientPendingComponent) entityManager.getComponentInEntity(entity, ClientPendingComponent.class);
			
			if (clientComponent.isDisconnected() || (pendingValidationComponent != null && Globals.now - pendingValidationComponent.getTimestamp() > 2000)) {
				clientComponent.disconnect();
				removed.add(entity);
				continue;
			}

			byte[] data;
			while ((data = clientComponent.getData()) != null) {
				DataPacket dataPacket = new DataPacket(data);
				byte type = dataPacket.getByte();
				byte identifier = dataPacket.getByte(); // Request identifier, used for response
				RequestType requestType = RequestType.values()[type & 0xFF];
				switch (requestType) {
				case CLIENT_REQUEST_PING: {
					DataPacket sendDataPacket = new DataPacket(new byte[5]);
					sendDataPacket.addByte((byte) RequestType.CLIENT_REQUEST_PING.ordinal());
					sendDataPacket.addByte((byte) identifier);
					sendDataPacket.addShort(Globals.tick);
					sendDataPacket.addByte((byte) 20);
					clientComponent.sendData(sendDataPacket.getData());
				}
					break;
				case CLIENT_REQUEST_READY: {
					if (pendingValidationComponent != null) {
						if (pendingValidationComponent.isValidated()) {
							entityManager.removeComponentAll(ClientPendingComponent.class, entity);
							entityManager.addComponent(new ClientValidatedComponent(), entity);
							ClientGameDataTransferComponent cgdtComponent = (ClientGameDataTransferComponent) entityManager.addComponent(new ClientGameDataTransferComponent(), entity);
							List<Entity> units = entityManager.getEntitiesContainingComponent(ObjectComponent.class);
							cgdtComponent.createUnits(units);
						} else {
							DataPacket sendDataPacket = new DataPacket(new byte[3]);
							sendDataPacket.addByte((byte) RequestType.CLIENT_REQUEST_READY.ordinal());
							sendDataPacket.addByte((byte) identifier);
							sendDataPacket.addByte((byte) 20);
							clientComponent.sendData(sendDataPacket.getData());
						}
					}
				}
					break;
				default:
					break;
				}
			}
		}
		for (Entity entity : removed)
			entityManager.removeEntity(entity);
	}

	@Override
	public void detach(Entity entity) {
		ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(entity, ClientComponent.class);
		uuids.remove(entity);
		clients.remove(clientComponent.getUuid());
	}
}
