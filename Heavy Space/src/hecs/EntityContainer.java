package hecs;

public interface EntityContainer {
	
	/**
	 * This method is called by the EntityManager when an Entity is removed.
	 * The EntityContainer must implement this method, which should the reference it has to the given entity, if it has a reference.
	 * 
	 * @param entity - the entity that the container removes a reference for.
	 */
	void detach(Entity entity);

}