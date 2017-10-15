package client.models;

import client.entities.Actor;
import client.gameData.ModelAttachementTag;

public class ModelAttachment {
	private Actor attachedActor;
	private Actor attachToActor;
	private ModelAttachementTag modelAttachementTag;

	public ModelAttachment(Actor attachedActor, Actor attachToActor, ModelAttachementTag modelAttachementTag) {
		this.attachedActor = attachedActor;
		this.attachToActor = attachToActor;
		this.modelAttachementTag = modelAttachementTag;
	}

	public Actor getAttachedActor() {
		return attachedActor;
	}

	public Actor getAttachToActor() {
		return attachToActor;
	}

	public ModelAttachementTag getModelAttachementTag() {
		return modelAttachementTag;
	}
}
