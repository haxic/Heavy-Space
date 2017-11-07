package client.models;

import client.components.ActorComponent;
import client.gameData.ModelAttachementTag;

public class ModelAttachment {
	private ActorComponent attachedActor;
	private ActorComponent attachToActor;
	private ModelAttachementTag modelAttachementTag;

	public ModelAttachment(ActorComponent attachedActor, ActorComponent attachToActor, ModelAttachementTag modelAttachementTag) {
		this.attachedActor = attachedActor;
		this.attachToActor = attachToActor;
		this.modelAttachementTag = modelAttachementTag;
	}

	public ActorComponent getAttachedActor() {
		return attachedActor;
	}

	public ActorComponent getAttachToActor() {
		return attachToActor;
	}

	public ModelAttachementTag getModelAttachementTag() {
		return modelAttachementTag;
	}
}
