package hecs;


public abstract class HECSSystem {
	protected HECSManager m;

	public HECSSystem(HECSManager m) {
		this.m = m;
	}
	
	public abstract void update(float delta);

}