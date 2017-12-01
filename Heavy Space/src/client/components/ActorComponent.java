package client.components;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import client.gameData.ModelAttachementTag;
import client.models.Model;
import client.models.ModelAttachment;
import client.models.ModelAttachmentPoint;
import hecs.EntityComponent;
import hecs.EntityContainer;
import hecs.Entity;

public class ActorComponent extends EntityComponent {

	// TODO: Make the actor component contain an Actor object which holds all attributes below
	private Model model;
	private Map<ModelAttachementTag, ModelAttachment> attachments = new HashMap<>();
	private ModelAttachment attachedTo;
	private int textureIndex = 0;
	private Vector2f textureOffset;

	public ActorComponent(Model model) {
		this.model = model;
		textureOffset = new Vector2f(calculateTextureXOffset(), calculateTextureYOffset());
	}

	public ActorComponent(Model model, int textureIndex) {
		this.model = model;
		this.textureIndex = textureIndex;
		textureOffset = new Vector2f(calculateTextureXOffset(), calculateTextureYOffset());
	}

	public Model getModel() {
		return model;
	}

	public boolean attachAnObject(ActorComponent attachedActor, ModelAttachementTag modelAttachementTag) {
		if (attachments.containsKey(modelAttachementTag)) {
			// TODO: Don't use syso!
			System.out.println("Warning: tried to attach to a busy attachment point!");
			return false;
		} else if (attachedTo != null) {
			// TODO: Don't use syso!
			System.out.println("Warning: tried to attach to an actor that is attached to something!");
			return false;
		}
		ModelAttachment modelAttachment = new ModelAttachment(attachedActor, this, modelAttachementTag);
		attachments.put(modelAttachementTag, modelAttachment);
		attachedActor.attach(modelAttachment);
		return true;
	}

	private void attach(ModelAttachment modelAttachment) {
		attachedTo = modelAttachment;
	}

	public boolean isAttached() {
		return attachedTo != null;
	}

	public ModelAttachment getModelAttachment() {
		return attachedTo;
	}

	public ModelAttachmentPoint getAttachementPoint() {
		return attachedTo.getAttachToActor().getModel().getAttachementPoint(attachedTo.getModelAttachementTag());
	}

	private float calculateTextureXOffset() {
		int column = textureIndex % model.getTexture().getAtlasSize();
		return (float) column / (float) model.getTexture().getAtlasSize();
	}

	private float calculateTextureYOffset() {
		int row = textureIndex / model.getTexture().getAtlasSize();
		return (float) row / (float) model.getTexture().getAtlasSize();
	}

	public Vector2f getTextureOffset() {
		return textureOffset;
	}

	public void setTextureOffset(Vector2f textureOffset) {
		this.textureOffset = textureOffset;
	}

	@Override
	protected void removeComponent() {
		// TODO Auto-generated method stub
	}

}
