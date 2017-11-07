package hecs;

public interface HECSContainer extends HECSComponent {
	void detach(HECSEntity entity);

	void attach(HECSEntity entity);
}