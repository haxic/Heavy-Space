package client.systems;

import java.util.List;

import client.components.ActorComponent;
import client.components.LightComponent;
import client.components.ParticleComponent;
import client.components.SnapshotComponent;
import client.gameData.ClientGameFactory;
import client.gameData.Scene;
import client.network.ConnectionManager;
import gameServer.components.ShipComponent;
import hecs.Entity;
import hecs.EntityManager;
import shared.components.ObjectComponent;
import shared.components.ProjectileComponent;
import shared.components.SpawnComponent;
import shared.functionality.Event;
import shared.functionality.EventHandler;
import shared.functionality.EventType;
import shared.functionality.Globals;

public class SpawnSystem {

	private EntityManager entityManager;
	private Scene scene;
	private ConnectionManager connectionManager;
	private EventHandler eventHandler;
	private ClientGameFactory clientGameFactory;

	public SpawnSystem(EntityManager entityManager, Scene scene, ConnectionManager connectionManager, EventHandler eventHandler, ClientGameFactory clientGameFactory) {
		this.entityManager = entityManager;
		this.scene = scene;
		this.connectionManager = connectionManager;
		this.eventHandler = eventHandler;
		this.clientGameFactory = clientGameFactory;
	}

	public void process() {
		List<Entity> entities = entityManager.getEntitiesContainingComponent(SpawnComponent.class);
		if (entities == null)
			return;
		if (entities.isEmpty())
			return;
		for (Entity entity : entities) {
			SpawnComponent spawnComponent = (SpawnComponent) entityManager.getComponentInEntity(entity, SpawnComponent.class);
			if (Globals.tick >= spawnComponent.getTick()) {
				clientGameFactory.spawnEntity(entity, spawnComponent);
				ShipComponent shipComponent = (ShipComponent) entityManager.getComponentInEntity(entity, ShipComponent.class);
				
				if (shipComponent != null && spawnComponent.getOwnerEntityID() == connectionManager.getPlayerID()) {
					System.out.println("DONT RENDER OWN SHIP!");
					eventHandler.addEvent(new Event(EventType.CLIENT_EVENT_SPAWN_PLAYER_SHIP, entity));
				} else {
					ActorComponent actor = (ActorComponent) entityManager.getComponentInEntity(entity, ActorComponent.class);
					if (actor != null)
						scene.addActorEntity(entity);
					ParticleComponent particle = (ParticleComponent) entityManager.getComponentInEntity(entity, ParticleComponent.class);
					if (particle != null)
						scene.addParticleEntity(entity);
					LightComponent light = (LightComponent) entityManager.getComponentInEntity(entity, LightComponent.class);
					if (light != null)
						scene.addLightEntity(entity);
				}
			}
		}
	}
}
