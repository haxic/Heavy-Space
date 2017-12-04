package shared.functionality;

public class BoundingBox {
	public float xMin;
	public float xMax;
	public float yMin;
	public float yMax;
	public float zMin;
	public float zMax;

	@Override
	public String toString() {
		return xMin + "-" + xMax + " " + yMin + "-" + yMax + " " + zMin + "-" + zMax;
	}

	public String toStringAsInts() {
		return "(" + (int) xMin + ":" + (int) xMax + " " + (int) yMin + ":" + (int) yMax + " " + (int) zMin + ":" + (int) zMax + ")";
	}
}
