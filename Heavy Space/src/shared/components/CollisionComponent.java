package shared.components;

import org.joml.Vector3f;

import hecs.EntityComponent;
import shared.functionality.BoundingBox;

public class CollisionComponent extends EntityComponent {
	private Vector3f previousPosition;
	private float radius;
	private BoundingBox boundingBox;

	public CollisionComponent(float radius) {
		this.radius = radius;
		previousPosition = new Vector3f();
		boundingBox = new BoundingBox();
	}

	@Override
	protected void removeComponent() {
	}

	public Vector3f getPreviousPosition() {
		return previousPosition;
	}

	public float getRadius() {
		return radius;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
}