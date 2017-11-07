package hecs;

import java.util.ArrayList;

public class Entity {
	ArrayList<EntityContainer> references = new ArrayList<EntityContainer>();
	private long eid;

	public Entity(long eid) {
		this.eid = eid;
	}

	public void attach(EntityContainer component) {
		references.add(component);
	}

	public void detach(EntityContainer component) {
		references.remove(component);
	}

	public long getEID() {
		return eid;
	}

}