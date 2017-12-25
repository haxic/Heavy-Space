package hecs;

import java.util.HashSet;
import java.util.Set;

public class Entity {
	protected Set<EntityContainer> references = new HashSet<>();
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

	@Override
	public String toString() {
		return "(" + eid + ")";
	}

}