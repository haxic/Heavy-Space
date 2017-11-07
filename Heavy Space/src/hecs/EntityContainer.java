package hecs;

public interface EntityContainer extends EntityComponent {
	
	/**
	 * This method must be called whenever the container removes a reference of an entity.
	 * 
	 * @param entity - the entity that the container removes a reference for.
	 */
	void detach(Entity entity);

	/**
	 * This method must be called whenever the container creates a reference to an entity. 
	 * 
	 * @param entity - the entity that the container creates a reference to.
	 */
	void attach(Entity entity);

	/**
	 * (Ignore this method unless extending EntityComponent!)
	 * 
	 * This method is called when the container is a component that is being removed by the entity manager.
	 * It must detach all entities that it references to.
	 */
	void cleanUp();
}