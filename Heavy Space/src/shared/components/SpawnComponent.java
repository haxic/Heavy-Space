package shared.components;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import hecs.EntityComponent;

public class SpawnComponent extends EntityComponent {

	private boolean instant;
	private short tick;
	private int entityType;
	private int entityVariation;
	private short ownerEntityID;
	private Vector3f position;
	private Quaternionf orientation;
//	private Vector3f forward;
//	private Vector3f up;
//	private Vector3f right;
	private Vector3f velocity;

//	public SpawnComponent(short tick, int entityType, int entityVariation, short ownerEntityID, Vector3f position, Vector3f forward, Vector3f up, Vector3f right, Vector3f velocity) {
	public SpawnComponent(short tick, int entityType, int entityVariation, short ownerEntityID, Vector3f position, Quaternionf quaternionf, Vector3f velocity) {
		this.tick = tick;
		this.entityType = entityType;
		this.entityVariation = entityVariation;
		this.ownerEntityID = ownerEntityID;
		this.position = position;
		this.orientation = quaternionf;
//		this.forward = forward;
//		this.up = up;
//		this.right = right;
		this.velocity = velocity;
	}

	@Override
	protected void removeComponent() {
	}

	public short getTick() {
		return tick;
	}

	public boolean isInstant() {
		return instant;
	}

	public int getEntityType() {
		return entityType;
	}

	public int getEntityVariation() {
		return entityVariation;
	}

	public short getOwnerEntityID() {
		return ownerEntityID;
	}

	public Vector3f getPosition() {
		return position;
	}
	
	public Quaternionf getOrientation() {
		return orientation;
	}

//	public Vector3f getForward() {
//		return forward;
//	}
//
//	public Vector3f getUp() {
//		return up;
//	}
//
//	public Vector3f getRight() {
//		return right;
//	}

	public Vector3f getVelocity() {
		return velocity;
	}
}
