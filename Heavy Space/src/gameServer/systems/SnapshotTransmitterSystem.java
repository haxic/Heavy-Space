package gameServer.systems;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import gameServer.components.ClientComponent;
import gameServer.components.ClientGameDataTransferComponent;
import gameServer.components.ObstacleComponent;
import gameServer.components.PlayerComponent;
import gameServer.components.RemoveComponent;
import gameServer.components.ShipComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.DeathComponent;
import shared.components.MovementComponent;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.functionality.DataPacket;
import shared.functionality.network.RequestType;
import shared.functionality.network.UDPServer;
import utilities.BitConverter;

public class SnapshotTransmitterSystem {

	private EntityManager entityManager;
	private UDPServer udpServer;
	private short sstick;

	public SnapshotTransmitterSystem(EntityManager entityManager, UDPServer udpServer) {
		this.entityManager = entityManager;
		this.udpServer = udpServer;
	}

	public void process(int tick) {
		short nextSnapshot = (short) (((tick / 3) % Short.MAX_VALUE));
		// short nextSnapshot = (short) (Globals.tick / 3);
		if (nextSnapshot == sstick)
			return;
		sstick = nextSnapshot;
		List<Entity> clientEntities = entityManager.getEntitiesContainingComponent(ClientGameDataTransferComponent.class);
		if (clientEntities == null)
			return;
		if (clientEntities.isEmpty())
			return;

		for (Entity clientEntity : clientEntities) {
			ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(clientEntity, ClientComponent.class);
			if (clientComponent == null)
				continue;
			if (clientComponent.isDisconnected())
				continue;

			List<DataPacket> dataPackets = new ArrayList<DataPacket>();
			ClientGameDataTransferComponent cgdtComponent = (ClientGameDataTransferComponent) entityManager.getComponentInEntity(clientEntity, ClientGameDataTransferComponent.class);
			createUnits(dataPackets, cgdtComponent);
			updateUnits(dataPackets, cgdtComponent);

			// Make sure that snapshot tick always gets sent
			if (dataPackets.isEmpty()) {
				DataPacket dataPacket = new DataPacket(new byte[4]);
				dataPacket.addByte((byte) RequestType.SERVER_REPONSE_UPDATE.ordinal()); // 0, Request type
				dataPacket.addShort(sstick); // 1-2, Current game state
				dataPacket.addByte((byte) 20); // End of data
				dataPackets.add(dataPacket);
			}

			cgdtComponent.clear();
			// System.out.println("SEND DATA " + sstick);
			for (DataPacket dataPacket : dataPackets) {
				DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), clientComponent.getUDPAddress(), clientComponent.getUDPPort());
				// System.out.println(datagramPacket.getLength());
				udpServer.sendData(datagramPacket);
			}
		}
		List<Entity> spawnedEntities = entityManager.getEntitiesContainingComponent(SpawnComponent.class);
		if (spawnedEntities != null)
			for (Entity spawnedEntity : spawnedEntities) {
				entityManager.removeComponentAll(SpawnComponent.class, spawnedEntity);
			}
	}

	boolean[] tempBools = new boolean[8];

	// UPDATE OBJECTS
	private void updateUnits(List<DataPacket> dataPackets, ClientGameDataTransferComponent cgdtComponent) {
		List<Entity> updateUnits = entityManager.getEntitiesContainingComponent(ObjectComponent.class);

		if (updateUnits != null && !updateUnits.isEmpty())
			cgdtComponent.updateUnits(updateUnits);
		if (!cgdtComponent.getUpdateEntities().isEmpty()) {
			DataPacket dataPacket = createDataPacket((byte) RequestType.SERVER_REPONSE_UPDATE_ENTITIES.ordinal(), sstick, (byte) dataPackets.size());

			byte entityCounter = 0;
			for (Entity updateEntity : cgdtComponent.getUpdateEntities()) {
				ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(updateEntity, ProjectileComponent.class);
				MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(updateEntity, MovementComponent.class);
				DeathComponent death = (DeathComponent) entityManager.getComponentInEntity(updateEntity, DeathComponent.class);
				// Only update projectiles if they die
				if (projectile != null && death == null)
					continue;
				// Only update static objects if they die
				if (movement == null && death == null)
					continue;
				if (dataPacket.size() + 59 >= dataPacket.maxSize()) {
					dataPackets.add(closeDataPacket(entityCounter, dataPacket));

					// New data packet
					entityCounter = 0;
					dataPacket = createDataPacket((byte) RequestType.SERVER_REPONSE_UPDATE_ENTITIES.ordinal(), sstick, (byte) dataPackets.size());
				}

				ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(updateEntity, ObjectComponent.class);

				if (death != null) {
					entityManager.addComponent(new RemoveComponent(), updateEntity);
				}

				tempBools[0] = movement != null && projectile == null;
				tempBools[1] = death != null ? true : false;
				tempBools[2] = death != null ? death.getKillingEntity() != null : false;

				// System.out.println("SEND ENTITY " + tempBools[0] + " " + updateEntity.getEID() + " " + unit.getPosition().x + ":" +
				// unit.getPosition().y+ ":" + unit.getPosition().z);
				// 4 ints = 16 bytes
				dataPacket.addInteger((int) (updateEntity.getEID())); // 5-8, Entity id
				dataPacket.addByte(BitConverter.byteFromBooleanArray(tempBools)); // 9, Flags id
				if (tempBools[0]) {
					dataPacket.addFloat(objectComponent.getPosition().x); // 10-13, Position x
					dataPacket.addFloat(objectComponent.getPosition().y); // 14-17, Position y
					dataPacket.addFloat(objectComponent.getPosition().z); // 18-21, Position z

					dataPacket.addFloat(objectComponent.getOrientation().x); // 22-25, Orientation x
					dataPacket.addFloat(objectComponent.getOrientation().y); // 26-29, Orientation y
					dataPacket.addFloat(objectComponent.getOrientation().z); // 30-33, Orientation z
					dataPacket.addFloat(objectComponent.getOrientation().w); // 34-37, Orientation z
					
					// dataPacket.addFloat(objectComponent.getForward().x); // 22-25, Forward x
					// dataPacket.addFloat(objectComponent.getForward().y); // 26-29, Forward y
					// dataPacket.addFloat(objectComponent.getForward().z); // 30-33, Forward z
					//
					// dataPacket.addFloat(objectComponent.getUp().x); // 34-29, Up x
					// dataPacket.addFloat(objectComponent.getUp().y); // 38-41, Up y
					// dataPacket.addFloat(objectComponent.getUp().z); // 42-45, Up z
					//
					// dataPacket.addFloat(objectComponent.getRight().x); // 46-49, Right x
					// dataPacket.addFloat(objectComponent.getRight().y); // 50-53, Right y
					// dataPacket.addFloat(objectComponent.getRight().z); // 54-57, Right z
				}

				// If killed by another entity, add killing entity id
				if (tempBools[2]) {
					dataPacket.addInteger((int) (death.getKillingEntity().getEID())); // 58-61, Killing entity id
				}
				entityCounter++;
			}
			dataPackets.add(closeDataPacket(entityCounter, dataPacket));
		}
	}

	// UPDATE OBJECTS
	private void createUnits(List<DataPacket> dataPackets, ClientGameDataTransferComponent cgdtComponent) {
		List<Entity> createUnits = entityManager.getEntitiesContainingComponent(SpawnComponent.class);
		if (createUnits != null && !createUnits.isEmpty())
			cgdtComponent.createUnits(createUnits);
		if (!cgdtComponent.getCreateEntities().isEmpty()) {
			DataPacket dataPacket = createDataPacket(RequestType.SERVER_REPONSE_SPAWN_ENTITIES.asByte(), sstick, (byte) dataPackets.size());

			byte entityCounter = 0;
			for (Entity createEntity : cgdtComponent.getCreateEntities()) {
				if (dataPacket.size() + 58 >= dataPacket.maxSize()) {
					dataPackets.add(closeDataPacket(entityCounter, dataPacket));

					// New data packet
					entityCounter = 0;
					dataPacket = createDataPacket(RequestType.SERVER_REPONSE_SPAWN_ENTITIES.asByte(), sstick, (byte) dataPackets.size());
				}
				byte entityType;

				ProjectileComponent projectileComponent = (ProjectileComponent) entityManager.getComponentInEntity(createEntity, ProjectileComponent.class);
				ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(createEntity, ShipComponent.class);
				ObstacleComponent obstacleComponent = (ObstacleComponent) entityManager.getComponentInEntity(createEntity, ObstacleComponent.class);

				byte entityVariation = 0;
				short entityOwnerID = 0;

				if (shipComponent != null) {
					// SHIPS
					entityType = 0;
					PlayerComponent playerComponent = (PlayerComponent) entityManager.getComponentInEntity(shipComponent.getPlayer(), PlayerComponent.class);
					if (playerComponent != null)
						entityOwnerID = playerComponent.getPlayerID();
				} else if (projectileComponent != null) {
					// PROJECTILES
					entityType = 1;
					entityVariation = projectileComponent.getVariation();
					entityOwnerID = (short) projectileComponent.getShipEntity().getEID();
				} else if (obstacleComponent != null) {
					// STATIC OBSTACLE
					entityType = 2;
				} else {
					entityType = 3;
				}

				ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(createEntity, ObjectComponent.class);
				SpawnComponent spawnComponent = (SpawnComponent) entityManager.getComponentInEntity(createEntity, SpawnComponent.class);
				Vector3f position = new Vector3f(0, 0, 0);
				if (spawnComponent != null) {
					position = spawnComponent.getPosition();
				} else if (objectComponent != null) {
					position = objectComponent.getPosition();
				}

				// 4 ints + 2 bytes = 18 bytes
				dataPacket.addInteger((int) (createEntity.getEID())); // 5, Entity id
				dataPacket.addByte(entityType); // 9, Entity type (ship, projectile, obstacle etc)
				dataPacket.addByte(entityVariation); // 10, Entity variation (what variation of the type)
				if (entityType != 2)
					dataPacket.addShort(entityOwnerID); // 11-12, Player id
				dataPacket.addFloat(position.x); // 13-16, Position x
				dataPacket.addFloat(position.y); // 17-20, Position y
				dataPacket.addFloat(position.z); // 21-24, Position z
				if (entityType == 1) {
					MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(createEntity, MovementComponent.class);
					Vector3f linearVel = movement.getLinearVel();
					dataPacket.addFloat(linearVel.x); // 25-28, Velocity x
					dataPacket.addFloat(linearVel.y); // 29-32, Velocity y
					dataPacket.addFloat(linearVel.z); // 33-36, Velocity z
				}
				if (entityType != 1) {
					dataPacket.addFloat(objectComponent.getOrientation().x); // 22-25, Orientation x
					dataPacket.addFloat(objectComponent.getOrientation().y); // 26-29, Orientation y
					dataPacket.addFloat(objectComponent.getOrientation().z); // 30-33, Orientation z
					dataPacket.addFloat(objectComponent.getOrientation().w); // 34-37, Orientation z
				}

				entityCounter++;
			}
			dataPackets.add(closeDataPacket(entityCounter, dataPacket));
		}
	}

	private DataPacket createDataPacket(byte requestType, short currentTick, byte packetNumber) {
		DataPacket dataPacket = new DataPacket(new byte[508]);
		dataPacket.addByte(requestType); // 0, Request type
		dataPacket.addShort(currentTick); // 1-2, Current game state
		dataPacket.addByte(packetNumber); // 3, Packet number
		dataPacket.addByte((byte) 0); // 4, Number of entities. Value is set by calling closeDataPacket
		return dataPacket;
	}

	private DataPacket closeDataPacket(byte numberOfEntities, DataPacket dataPacket) {
		// Close data packet
		if (numberOfEntities != 0)
			dataPacket.setByteAt(numberOfEntities, 4); // 3, Set number of entities.
		dataPacket.addByte((byte) 20); // End of data
		return dataPacket;
	}

	public short getTick() {
		return sstick;
	}

}
