package gameServer.events;

import org.joml.Vector3f;

import hecs.Entity;
import hevent.Event;

public class PlayerActionEvent extends Event {

	public Entity player;
	public boolean[] actions;
	public Vector3f angularVelocity;
	public float angularVelocityDT;
	public float dt;

	public PlayerActionEvent(Entity player, boolean[] actions, Vector3f angularVelocity, float angularVelocityDT, float dt) {
		super();
		this.player = player;
		this.actions = actions;
		this.angularVelocity = angularVelocity;
		this.angularVelocityDT = angularVelocityDT;
		this.dt = dt;
	}
}
