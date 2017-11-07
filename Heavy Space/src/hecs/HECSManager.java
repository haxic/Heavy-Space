package hecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HECSManager {
	private int lowestUnassignedEntityID;
	private Map<Class<? extends HECSComponent>, HashMap<HECSEntity, HECSComponent>> dataStructure;
	private List<HECSEntity> entities;

	public HECSManager() {
		entities = new ArrayList<HECSEntity>();
		dataStructure = new HashMap<Class<? extends HECSComponent>, HashMap<HECSEntity, HECSComponent>>();
		lowestUnassignedEntityID = 1;
	}

	/**
	 * This method generates and returns a unique entity ID.
	 */
	private long generateEntityID() {
//		System.out.println(lowestUnassignedEntityID);
		if (lowestUnassignedEntityID < Integer.MAX_VALUE) {
			return lowestUnassignedEntityID++;
		} else {
			for (long i = 1; i < Integer.MAX_VALUE; i++) {
				boolean isContaining = false;
				for (HECSEntity pickedEntity : entities) {
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
	public HECSEntity createEntity(HECSComponent... components) {
		long entityID = generateEntityID();
		HECSEntity entity = new HECSEntity(entityID);
		entities.add(entity);
		if (components != null)
			addComponents(components, entity);
		return entity;
	}

	/**
	 * This adds a list of components to an entity;
	 */
	public void addComponents(HECSComponent[] components, HECSEntity entity) {
		for (int i = 0; i < components.length; i++) {
			addComponent(components[i], entity);
		}
	}

	/**
	 * This method adds a selected component to a selected entity.
	 */
	public HECSComponent addComponent(HECSComponent component, HECSEntity entity) {
		HashMap<HECSEntity, HECSComponent> components = dataStructure.get(component.getClass());
		if (components == null) {
			components = new HashMap<HECSEntity, HECSComponent>();
			dataStructure.put(component.getClass(), components);
		}
		components.put(entity, component);
		return component;
	}

	/**
	 * This method deletes a selected entity and all of its components from the
	 * entire data structure.
	 */
	public void removeEntity(HECSEntity entity) {
		if (!entities.contains(entity))
			return;
		for (Entry<Class<? extends HECSComponent>, HashMap<HECSEntity, HECSComponent>> entry : dataStructure
				.entrySet()) {
			boolean containsEntity = entry.getValue().containsKey(entity);
			if (containsEntity) {
				entry.getValue().remove(entity);
			}
		}
		for (HECSContainer component : entity.references) {
			component.detach(entity);
		}
		entities.remove(entity);
	}

	/**
	 * This method removes all instances of s selected component class type from
	 * a selected entity.
	 */
	public void removeComponentAll(Class<? extends HECSComponent> componentClass, HECSEntity entity) {
		HashMap<HECSEntity, HECSComponent> componentsOfEntity = dataStructure.get(componentClass);
		if (componentsOfEntity == null)
			return;
		while (componentsOfEntity.containsKey(entity)) {
			componentsOfEntity.remove(entity);
		}
	}

	/**
	 * This method removes all instances of all component class types in the
	 * selected component class type list from a selected entity.
	 */
	public void removeMultipleComponents(Class<? extends HECSComponent>[] componentClass, HECSEntity entity) {
		if (componentClass != null)
			for (int i = 0; i < componentClass.length; i++) {
				removeComponentAll(componentClass[i], entity);
			}
	}

	/**
	 * This method attempts to remove any one instance of a selected component
	 * class type from a selected entity.
	 */
	public void removeComponentOnce(Class<? extends HECSComponent> componentClass, HECSEntity entity) {
		HashMap<HECSEntity, HECSComponent> componentsOfEntity = dataStructure.get(componentClass);
		if (componentsOfEntity == null)
			return;
		componentsOfEntity.remove(entity);
	}

	/**
	 * This method returns a list of all entities that contains a selected
	 * component class type.
	 */
	public List<HECSEntity> getEntitiesContainingComponent(Class<? extends HECSComponent> componentClass) {
		HashMap<HECSEntity, HECSComponent> components = dataStructure.get(componentClass);

		if (components != null) {
			List<HECSEntity> containingEntities = new ArrayList<HECSEntity>();
			for (Entry<HECSEntity, HECSComponent> entry : components.entrySet()) {
				containingEntities.add(entry.getKey());
			}
			return containingEntities;
		} else {
			return null;
		}
	}

	/**
	 * This method attempts to return one entity that contains a instance of a
	 * selected component class type.
	 */
	public HECSEntity getEntityContainingComponentOfClass(Class<? extends HECSComponent> componentClass) {
		HashMap<HECSEntity, HECSComponent> components = dataStructure.get(componentClass);
		if (components == null)
			return null;
		for (Entry<HECSEntity, HECSComponent> entry : components.entrySet()) {
			return entry.getKey();
		}
		return null;
	}

	/**
	 * This method attempts to return a selected component class type contains
	 * by any one entity that contains an instance of it.
	 */
	public HECSComponent getComponentOfClassContainingSameComponentOfClass(
			Class<? extends HECSComponent> componentClass) {
		return dataStructure.get(componentClass).get(getEntityContainingComponentOfClass(componentClass));
	}

	public HECSComponent getComponentInEntity(HECSEntity entity, Class<? extends HECSComponent> componentClass) {
		return dataStructure.get(componentClass).get(entity);
	}

	/**
	 * This method attempts to return a instance of a selected component class
	 * type A from any one entity that contains a selected component class type
	 * B.
	 */
	public HECSComponent getComponentOfClassOfEntityContainingDifferentComponentOfClass(
			Class<? extends HECSComponent> componentClass1, Class<? extends HECSComponent> componentClass2) {
		return dataStructure.get(componentClass2).get(getEntityContainingComponentOfClass(componentClass1));
	}
}