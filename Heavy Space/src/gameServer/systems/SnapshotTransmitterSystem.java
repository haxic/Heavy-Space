package gameServer.systems;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import shared.functionality.SnapshotSequenceType;
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

	List<DataPacket> dataPackets = new ArrayList<DataPacket>();
	DataPacket dataPacket;

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

		for (Entity entity : clientEntities) {
			ClientComponent clientComponent = (ClientComponent) entityManager.getComponentInEntity(entity, ClientComponent.class);
			if (clientComponent == null)
				continue;
			if (clientComponent.isDisconnected())
				continue;

			ClientGameDataTransferComponent cgdtComponent = (ClientGameDataTransferComponent) entityManager.getComponentInEntity(entity, ClientGameDataTransferComponent.class);
			createDataPacket();
			sendEntities(cgdtComponent.getCreateEntities(), 33, SnapshotSequenceType.CREATE);
			sendEntities(cgdtComponent.getUpdateEntities(), 36, SnapshotSequenceType.UPDATE);
			endPacket();

			cgdtComponent.clear();
			// System.out.println("SEND DATA " + sstick);
			for (DataPacket dataPacket : dataPackets) {
				DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), clientComponent.getUDPAddress(), clientComponent.getUDPPort());
				System.out.println(datagramPacket.getLength());
				udpServer.sendData(datagramPacket);
			}
			dataPackets.clear();
		}
	}

	byte entityCounter = 0;
	byte entityCounterPosition = 0;
	private byte packetNumber;

	// CREATE ENTITIES
	private void sendEntities(Set<Entity> entities, int entityMaxSize, SnapshotSequenceType snapshotSequenceType) {
		if (entities.isEmpty())
			return;
		if (dataPacket.size() + entityMaxSize + 1 >= dataPacket.maxSize()) {
			endPacket();
			createDataPacket();
		}
		createSequence(snapshotSequenceType);

		for (Entity entity : entities) {
			if (dataPacket.size() + entityMaxSize + 1 >= dataPacket.maxSize()) {
				endSequence();
				endPacket();
				createDataPacket();
				createSequence(snapshotSequenceType);
			}
			switch (snapshotSequenceType) {
			case CREATE:
				createEntity(entity);
				break;
			case UPDATE:
				updateEntity(entity);
				break;
			default:
				break;
			}
			entityCounter++;
		}
		endSequence();
	}

	private void createSequence(SnapshotSequenceType snapshotSequenceType) {
		dataPacket.addByte(snapshotSequenceType.asByte());
		entityCounter = 0;
		entityCounterPosition = (byte) dataPacket.size();
		dataPacket.addByte((byte) 0); // Number of entities - set in endSequence()
	}

	private void endSequence() {
		dataPacket.setByteAt(entityCounter, entityCounterPosition);
	}

	private void createDataPacket() {
		dataPacket = new DataPacket(new byte[508]);
		dataPacket.addByte(RequestType.SERVER_REPONSE_SNAPSHOT.asByte()); // 0, Request type
		dataPacket.addShort(sstick); // 1-2, Current game state
		dataPacket.addByte(packetNumber++); // 3, Packet number
	}

	private void endPacket() {
		dataPacket.addByte(SnapshotSequenceType.END.asByte()); // 0, Request type
		dataPacket.addByte((byte) 20); // End of data
		dataPackets.add(dataPacket);
	}

	public short getTick() {
		return sstick;
	}

	private void createEntity(Entity entity) {
		byte entityType;

		ProjectileComponent projectileComponent = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);
		ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(entity, ShipComponent.class);
		ObstacleComponent obstacleComponent = (ObstacleComponent) entityManager.getComponentInEntity(entity, ObstacleComponent.class);

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

		ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);
		SpawnComponent spawnComponent = (SpawnComponent) entityManager.getComponentInEntity(entity, SpawnComponent.class);
		Vector3f position = new Vector3f(0, 0, 0);
		if (spawnComponent != null) {
			position = spawnComponent.getPosition();
		} else if (objectComponent != null) {
			position = objectComponent.getPosition();
		}

		// 4 ints + 2 bytes = 18 bytes
		dataPacket.addInteger((int) (entity.getEID())); // 5, Entity id
		dataPacket.addByte(entityType); // 9, Entity type (ship, projectile, obstacle etc)
		dataPacket.addByte(entityVariation); // 10, Entity variation (what variation of the type)
		if (entityType != 2)
			dataPacket.addShort(entityOwnerID); // 11-12, Player id
		dataPacket.addFloat(position.x); // 13-16, Position x
		dataPacket.addFloat(position.y); // 17-20, Position y
		dataPacket.addFloat(position.z); // 21-24, Position z
		if (entityType == 1) {
			MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
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
	}

	boolean[] tempBools = new boolean[8];

	private void updateEntity(Entity entity) {
		ProjectileComponent projectile = (ProjectileComponent) entityManager.getComponentInEntity(entity, ProjectileComponent.class);
		MovementComponent movement = (MovementComponent) entityManager.getComponentInEntity(entity, MovementComponent.class);
		DeathComponent death = (DeathComponent) entityManager.getComponentInEntity(entity, DeathComponent.class);
		ObjectComponent objectComponent = (ObjectComponent) entityManager.getComponentInEntity(entity, ObjectComponent.class);

		if (death != null) {
			entityManager.addComponent(new RemoveComponent(), entity);
		}

		tempBools[0] = movement != null && projectile == null;
		tempBools[1] = death != null ? true : false;
		tempBools[2] = death != null ? death.getKillingEntity() != null : false;

		// System.out.println("SEND ENTITY " + tempBools[0] + " " + entity.getEID() + " " + unit.getPosition().x + ":" +
		// unit.getPosition().y+ ":" + unit.getPosition().z);
		// 4 ints = 16 bytes
		dataPacket.addInteger((int) (entity.getEID())); // 5-8, Entity id
		dataPacket.addByte(BitConverter.byteFromBooleanArray(tempBools)); // 9, Flags id
		if (tempBools[0]) {
			dataPacket.addFloat(objectComponent.getPosition().x); // 10-13, Position x
			dataPacket.addFloat(objectComponent.getPosition().y); // 14-17, Position y
			dataPacket.addFloat(objectComponent.getPosition().z); // 18-21, Position z

			dataPacket.addFloat(objectComponent.getOrientation().x); // 22-25, Orientation x
			dataPacket.addFloat(objectComponent.getOrientation().y); // 26-29, Orientation y
			dataPacket.addFloat(objectComponent.getOrientation().z); // 30-33, Orientation z
			dataPacket.addFloat(objectComponent.getOrientation().w); // 34-37, Orientation z
		}

		// If killed by another entity, add killing entity id
		if (tempBools[2]) {
			dataPacket.addInteger((int) (death.getKillingEntity().getEID())); // 38-41, Killing entity id
		}
	}

}
