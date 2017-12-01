package hecs;

public interface EntityContainer {
	
	/**
	 * This method must be called whenever the container removes a reference of an entity.
	 * 
	 * @param entity - the entity that the container removes a reference for.
	 */
	void detach(Entity entity);

}