package utilities;

public class ModelData {

	private float[] vertices;
	private float[] uvs;
	private float[] normals;
	private int[] indices;
	private float furthestPoint;

	public ModelData(float[] vertices, float[] uvs, float[] normals, int[] indices, float furthestPoint) {
		this.vertices = vertices;
		this.uvs = uvs;
		this.normals = normals;
		this.indices = indices;
		this.furthestPoint = furthestPoint;
	}

	public float[] getVertices() {
		return vertices;
	}

	public float[] getUVs() {
		return uvs;
	}

	public float[] getNormals() {
		return normals;
	}

	public int[] getIndices() {
		return indices;
	}

	public float getFurthestPoint() {
		return furthestPoint;
	}

}
