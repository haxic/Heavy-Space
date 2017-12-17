package hecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EntityManager {
	private int lowestUnassignedEntityID;
	private Map<Class<? extends EntityComponent>, HashMap<Entity, EntityComponent>> dataStructure;
	private List<Entity> entities;
	private int componentCounter = 0;

	public EntityManager() {
		entities = new ArrayList<Entity>();
		dataStructure = new HashMap<Class<? extends EntityComponent>, HashMap<Entity, EntityComponent>>();
		lowestUnassignedEntityID = 1;
	}

	public int numberOfEntities() {
		return entities.size();
	}

	public int numberOfComponents() {
		return componentCounter;
	}

	/**
	 * This method generates and returns a unique entity ID.
	 */
	private long generateEntityID() {
		if (lowestUnassignedEntityID < Integer.MAX_VALUE) {
			return lowestUnassignedEntityID++;
		} else {
			for (long i = 1; i < Integer.MAX_VALUE; i++) {
				boolean isContaining = false;
				for (Entity pickedEntity : entities) {
					if (pickedEntity.getEID() == i) {
						isContaining = true;
						break;
					}
				}
				if (!isContaining)
					return i;
			}
			System.out.println("ERROR: No available OID's!");
			System.exit(0);
			return 0;
		}

	}

	/*
	 * ******************** *
	 * 
	 * PUBLIC METHODS BELOW *
	 * 
	 * ******************** *
	 */

	/**
	 * This method creates and returns a new entity object.
	 */
	public Entity createEntity(EntityComponent... components) {
		long entityID = generateEntityID();
		Entity entity = new Entity(entityID);
		entities.add(entity);
		if (components != null)
			addComponents(components, entity);
		return entity;
	}

	/**
	 * This adds a list of components to an entity;
	 */
	public void addComponents(EntityComponent[] components, Entity entity) {
		for (int i = 0; i < components.length; i++) {
			addComponent(components[i], entity);
		}
	}

	/**
	 * This method adds a selected component to a selected entity.
	 */
	public EntityComponent addComponent(EntityComponent component, Entity entity) {
		HashMap<Entity, EntityComponent> components = dataStructure.get(component.getClass());
		if (components == null) {
			components = new HashMap<Entity, EntityComponent>();
			dataStructure.put(component.getClass(), components);
		}
		components.put(entity, component);
		componentCounter++;
		return component;
	}

	/**
	 * This method deletes a selected entity and all of its components from the entire data structure.
	 */
	public void removeEntity(Entity entity) {
		if (!entities.contains(entity))
			return;
		// Detach entity from all containers
		// TODO: Improve this. LOL!
		EntityContainer[] containers = new EntityContainer[entity.references.size()];
		for (int i = 0; i < containers.length; i++) {
			containers[i] = entity.references.get(i);
		}
		for (int i = 0; i < containers.length; i++) {
			containers[i].detach(entity);
		}
		// Remove all components belonging to the entity
		for (Entry<Class<? extends EntityComponent>, HashMap<Entity, EntityComponent>> entry : dataStructure.entrySet()) {
			HashMap<Entity, EntityComponent> value = entry.getValue();
			if (value.containsKey(entity)) {
				EntityComponent component = value.get(entity);
				component.removeComponent();
				value.remove(entity);
				componentCounter--;
			}
		}
		entities.remove(entity);
	}

	/**
	 * This method removes all instances of s selected component class type from a selected entity.
	 */
	public void removeComponentAll(Class<? extends EntityComponent> componentClass, Entity entity) {
		HashMap<Entity, EntityComponent> componentsOfEntity = dataStructure.get(componentClass);
		if (componentsOfEntity == null)
			return;
		while (componentsOfEntity.containsKey(entity)) {
			componentsOfEntity.remove(entity);
		}
	}

	/**
	 * This method removes all instances of all component class types in the selected component class type list from a selected entity.
	 */
	public void removeMultipleComponents(Class<? extends EntityComponent>[] componentClass, Entity entity) {
		if (componentClass != null)
			for (int i = 0; i < componentClass.length; i++) {
				removeComponentAll(componentClass[i], entity);
			}
	}

	/**
	 * This method attempts to remove any one instance of a selected component class type from a selected entity.
	 */
	public void removeComponentOnce(Class<? extends EntityComponent> componentClass, Entity entity) {
		HashMap<Entity, EntityComponent> componentsOfEntity = dataStructure.get(componentClass);
		if (componentsOfEntity == null)
			return;
		componentsOfEntity.remove(entity);
	}

	/**
	 * This method returns a list of all entities that contains a selected component class type.
	 */
	public List<Entity> getEntitiesContainingComponent(Class<? extends EntityComponent> componentClass) {
		HashMap<Entity, EntityComponent> components = dataStructure.get(componentClass);
		if (components != null) {
			List<Entity> containingEntities = new ArrayList<Entity>();
			for (Entry<Entity, EntityComponent> entry : components.entrySet()) {
				containingEntities.add(entry.getKey());
			}
			return containingEntities;
		} else {
			return null;
		}
	}

	public int sizeEntitiesContainingComponent(Class<? extends EntityComponent> componentClass) {
		HashMap<Entity, EntityComponent> components = dataStructure.get(componentClass);
		if (components == null)
			return -1;
		return components.size();
	}

	/**
	 * This method attempts to return one entity that contains a instance of a selected component class type.
	 */
	public Entity getEntityContainingComponentOfClass(Class<? extends EntityComponent> componentClass) {
		HashMap<Entity, EntityComponent> components = dataStructure.get(componentClass);
		if (components == null)
			return null;
		for (Entry<Entity, EntityComponent> entry : components.entrySet()) {
			return entry.getKey();
		}
		return null;
	}

	/**
	 * This method attempts to return a selected component class type contains by any one entity that contains an instance of it.
	 */
	public EntityComponent getComponentOfClassContainingSameComponentOfClass(Class<? extends EntityComponent> componentClass) {
		return dataStructure.get(componentClass).get(getEntityContainingComponentOfClass(componentClass));
	}

	public EntityComponent getComponentInEntity(Entity entity, Class<? extends EntityComponent> componentClass) {
		HashMap<Entity, EntityComponent> map = dataStructure.get(componentClass);
		if (map == null)
			return null;
		return map.get(entity);
	}

	/**
	 * This method attempts to return a instance of a selected component class type A from any one entity that contains a selected component class
	 * type B.
	 */
	public EntityComponent getComponentOfClassOfEntityContainingDifferentComponentOfClass(Class<? extends EntityComponent> componentClass1, Class<? extends EntityComponent> componentClass2) {
		return dataStructure.get(componentClass2).get(getEntityContainingComponentOfClass(componentClass1));
	}

	public int getSize() {
		return entities.size();
	}
}