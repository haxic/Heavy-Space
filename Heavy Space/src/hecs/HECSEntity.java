package hecs;

import java.util.ArrayList;

public class HECSEntity {
	ArrayList<HECSContainer> references = new ArrayList<HECSContainer>();
	private long eid;

	public HECSEntity(long eid) {
		this.eid = eid;
	}

	public void attach(HECSContainer component) {
		references.add(component);
	}

	public void detach(HECSContainer component) {
		references.remove(component);
	}

	public long getEID() {
		return eid;
	}

}