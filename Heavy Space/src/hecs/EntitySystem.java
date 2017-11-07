package hecs;

public abstract class EntitySystem {
	protected EntityManager entityManager;

	public EntitySystem(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

}