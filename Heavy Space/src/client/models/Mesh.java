package client.models;

public class Mesh {
	private int vaoID;
	private int indicesSize;

	public Mesh(int vaoID, int indicesSize) {
		this.vaoID = vaoID;
		this.indicesSize = indicesSize;
	}

	public int getVaoID() {
		return vaoID;
	}
	
	public int getIndicesSize() {
		return indicesSize;
	}
}
