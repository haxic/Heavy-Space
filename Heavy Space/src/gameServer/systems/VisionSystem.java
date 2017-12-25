package gameServer.systems;

import java.util.List;

import gameServer.components.ClientGameDataTransferComponent;
import gameServer.components.ShipComponent;
import gameServer.events.EntityCreatedEvent;
import hecs.Entity;
import hecs.EntityManager;
import hevent.Event;
import hevent.EventListener;
import hevent.EventManager;

public class VisionSystem implements EventListener {

	private EntityManager entityManager;
	private EventManager eventManager;

	public VisionSystem(EntityManager entityManager, EventManager eventManager) {
		this.entityManager = entityManager;
		this.eventManager = eventManager;
		eventManager.subscribe(EntityCreatedEvent.class, this);
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(VisionComponent.class);
		if (entities == null)
			return;
		for (Entity entity : entities) {
			VisionComponent visionComponent = (VisionComponent) entityManager.getComponentInEntity(entity, VisionComponent.class);
			ClientGameDataTransferComponent clientGameDataTransferComponent = (ClientGameDataTransferComponent) entityManager.getComponentInEntity(entity, ClientGameDataTransferComponent.class);
			System.out.println("VISIONSYSTEM.PROCESS");
			
			
			for (Entity createEntity : clientGameDataTransferComponent.getCreateEntities()) {
				ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(createEntity, ShipComponent.class);
				if (shipComponent != null) {
					visionComponent.updateEntity(createEntity);
					clientGameDataTransferComponent.updateEntity(createEntity);
				}
			}
			
			// Send created entities
			clientGameDataTransferComponent.clearCreateEntities();
			System.out.println(clientGameDataTransferComponent.getCreateEntities());
			clientGameDataTransferComponent.createEntities(visionComponent.getCreateEntities());
			System.out.println(clientGameDataTransferComponent.getCreateEntities());
			visionComponent.clearCreateEntities();
			System.out.println(visionComponent.getCreateEntities());
			

			
		}
	}

	// TODO: improve by stacking events and apply them all at the same time or something in the process method.
	@Override
	public void handleEvent(Event event) {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(VisionComponent.class);
		if (entities == null)
			return;
		EntityCreatedEvent entityCreatedEvent = (EntityCreatedEvent) event;
		for (Entity entity : entities) {
			VisionComponent visionComponent = (VisionComponent) entityManager.getComponentInEntity(entity, VisionComponent.class);
			visionComponent.createEntity(entityCreatedEvent.getEntity());
			System.out.println("EntityEvent: " + entityCreatedEvent.getEntity());
		}
	}

}
