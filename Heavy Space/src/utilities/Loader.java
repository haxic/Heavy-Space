package utilities;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import client.models.Mesh;
import client.models.Model;
import client.models.Texture;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class Loader {

	// Used to keep track of created VAOs and VBOs.
	private List<Integer> vaos = new ArrayList<Integer>();
	private Map<Integer, List<Integer>> vboReferences = new HashMap<Integer, List<Integer>>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();

	public final static String[] CUBE_SIDES_ORDER = { "right", "left", "top", "bottom", "back", "front" };
	private static final float[] QUAD_VERTICES = { -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f };

	public Mesh loadToVAO(ModelData modelData) {
		return loadToVAO(modelData.getVertices(), modelData.getUVs(), modelData.getNormals(), modelData.getIndices());
	}

	public void removeMesh(int vaoID) {
		for (Integer vboID : vboReferences.get(vaoID)) {
			GL30.glDeleteVertexArrays(vboID);
			// System.out.println("DELETED VBO: " + vboID + " in VAO: " + vaoID);
		}
		vboReferences.remove(vaoID);
		GL30.glDeleteVertexArrays(vaoID);
		vaos.remove(vaoID);
		// System.out.println("DELETED VAO: " + vaoID);
	}

	/**
	 * Deletes all VAOs and VBOs from memory.
	 */
	public void cleanUp() {
		int vbosCounted = 0;
		for (Entry<Integer, List<Integer>> entry : vboReferences.entrySet())
			for (Integer vboID : entry.getValue()) {
				vbosCounted++;
				GL30.glDeleteVertexArrays(vboID);
			}
		for (int vaoID : vaos)
			GL30.glDeleteVertexArrays(vaoID);
		for (int textureID : textures)
			GL11.glDeleteTextures(textureID);
		// TODO: Don't use syso!
		if (vbosCounted != vbos.size())
			System.out.println("WARNING: VBO miscounting! Counted: " + vbosCounted + ", but total list is: " + vbos.size() + "!");
		else
			System.out.println("Clean-up successful!");
	}

	// Load model
	public Mesh loadToVAO(float[] vertices, float[] uvs, float[] normals, int[] indices) {
		int vaoID = createVAO();
		vaos.add(vaoID);
		bindIndexBuffer(vaoID, indices);
		// Unbind VAO attributes.
		storeDataInAttributeList(vaoID, 0, 3, vertices);
		storeDataInAttributeList(vaoID, 1, 2, uvs);
		storeDataInAttributeList(vaoID, 2, 3, normals);
		// Unbind VAO.
		GL30.glBindVertexArray(0);
		return new Mesh(vaoID, indices.length);
	}

	// Load simple mesh (square)
	public Mesh loadToVAO(float[] positions, int dimensions) {
		int vaoID = createVAO();
		vaos.add(vaoID);
		storeDataInAttributeList(vaoID, 0, dimensions, positions);
		GL30.glBindVertexArray(0);
		return new Mesh(vaoID, positions.length / dimensions);
	}

	private int createVAO() {
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}

	/**
	 * Creates an empty VBO that can be used for instanced rendering
	 */
	public int createEmptyVBO(int floatCount) {
		// Create VBO.
		int vboID = GL15.glGenBuffers();
		// System.out.println("CREATED EMPTY VBO: " + vboID);
		vbos.add(vboID);
		// Bind VBO.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		// Allocate memory to the VBO and notify that this VBO is going to be updated often. "floatCount * 4" is the size in bytes.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatCount * 4, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vboID;
	}

	public void addInstancedAttribute(int vaoID, int vboID, int attributeNumber, int dataSize, int instancedDataLength, int offset) {
		// Create a reference to the VBO for the VAO.
		if (vboReferences.get(vaoID) == null)
			vboReferences.put(vaoID, new ArrayList<Integer>());
		if (!vboReferences.get(vaoID).contains(vboID))
			vboReferences.get(vaoID).add(vboID);
		// Bind VBO.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		// Bind VAO.
		GL30.glBindVertexArray(vaoID);
		// Store VBO in a VAO attribute list. "instancedDataLength * 4" and "offset * 4" are the sizes in bytes.
		GL20.glVertexAttribPointer(attributeNumber, dataSize, GL11.GL_FLOAT, false, instancedDataLength * 4, offset * 4);
		// Set VBO to be a per instance attribute.
		GL33.glVertexAttribDivisor(attributeNumber, 1);
		// Unbind VBO.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// Unbind VAO.
		GL30.glBindVertexArray(0);
	}

	/**
	 * The index buffer is not stored in the VAO attribute list. The VAO got a specific storage for the index buffer, and may only have one associated
	 * with it.
	 */
	private void bindIndexBuffer(int vaoID, int[] indices) {
		int vboID = GL15.glGenBuffers();
		if (vboReferences.get(vaoID) == null)
			vboReferences.put(vaoID, new ArrayList<Integer>());
		vboReferences.get(vaoID).add(vboID);
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = BufferStuff.getIntArrayAsIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	private void storeDataInAttributeList(int vaoID, int attributeNumber, int size, float[] data) {
		int vboID = GL15.glGenBuffers();
		if (vboReferences.get(vaoID) == null)
			vboReferences.put(vaoID, new ArrayList<Integer>());
		vboReferences.get(vaoID).add(vboID);
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = BufferStuff.getFloatArrayAsFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, size, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public Texture loadTexture(String fileName, int atlasSize, int texturePages) {
		int[] pixels = null;
		int width = 0;
		int height = 0;
		try {
			BufferedImage image = ImageIO.read(new FileInputStream("res/models/" + fileName + ".png"));
			width = image.getWidth();
			height = image.getHeight();
			pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int[] data = new int[width * height];
		for (int i = 0; i < width * height; i++) {
			int a = (pixels[i] & 0xff000000) >> 24;
			int r = (pixels[i] & 0xff0000) >> 16;
			int g = (pixels[i] & 0xff00) >> 8;
			int b = (pixels[i] & 0xff);

			data[i] = a << 24 | b << 16 | g << 8 | r;
		}

		int textureID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, BufferStuff.getIntArrayAsIntBuffer(data));
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0f);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_SMOOTH);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_SMOOTH);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		return new Texture(textureID, atlasSize, texturePages);
	}

	public Texture loadCubeMap(String folder) {
		// Generate empty texture.
		int textureID = GL11.glGenTextures();
		// System.out.println("CREATED TEXTURE: " + textureID);
		// Activate texture unit 0.
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		// Bind texture id to cube map.
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureID);
		// --T
		// L F R
		// --B
		// --B
		// Order: Right, Left, Top, Bottom, Back, Front
		for (int i = 0; i < CUBE_SIDES_ORDER.length; i++) {
			TextureData data = decodeTextureFile("res/skyboxes/" + folder + "/" + CUBE_SIDES_ORDER[i] + ".png");
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, data.width, data.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.buffer);
		}
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		// glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
		// glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
		textures.add(textureID);
		return new Texture(textureID);
	}

	public Model loadSkybox(String string, float size) {
		return new Model(loadToVAO(getCubeMapVertices(size), 3), loadCubeMap(string));
	}

	private TextureData decodeTextureFile(String fileName) {
		int width = 0;
		int height = 0;
		ByteBuffer buffer = null;
		try {
			FileInputStream in = new FileInputStream(fileName);
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * width * height);
			decoder.decode(buffer, width * 4, Format.RGBA);
			buffer.flip();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Tried to load texture " + fileName + ", didn't work.");
			System.exit(-1);
		}
		return new TextureData(width, height, buffer);
	}

	public static float[] getCubeMapVertices(float size) {
		return new float[] { -size, size, -size, -size, -size, -size, size, -size, -size, size, -size, -size, size, size, -size, -size, size, -size,

				-size, -size, size, -size, -size, -size, -size, size, -size, -size, size, -size, -size, size, size, -size, -size, size,

				size, -size, -size, size, -size, size, size, size, size, size, size, size, size, size, -size, size, -size, -size,

				-size, -size, size, -size, size, size, size, size, size, size, size, size, size, -size, size, -size, -size, size,

				-size, size, -size, size, size, -size, size, size, size, size, size, size, -size, size, size, -size, size, -size,

				-size, -size, -size, -size, -size, size, size, -size, -size, size, -size, -size, -size, -size, size, size, -size, size };
	}

	public static float[] getCubeVertices(float size) {
		return new float[] { 	
				-size,size,-size,	// far upper left
				-size,-size,-size,	// 
				size,-size,-size,	
				size,size,-size,		
				
				-size,size,size,	
				-size,-size,size,	
				size,-size,size,	
				size,size,size,
				
				size,size,-size,	
				size,-size,-size,	
				size,-size,size,	
				size,size,size,
				
				-size,size,-size,	
				-size,-size,-size,	
				-size,-size,size,	
				-size,size,size,
				
				-size,size,size,
				-size,size,-size,
				size,size,-size,
				size,size,size,
				
				-size,-size,size,
				-size,-size,-size,
				size,-size,-size,
				size,-size,size
				};
	}

	public static float[] getCubeUVs() {
		return new float[] { 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0 };
	}

	public static float[] getCubeMapNormals() {
		return new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
//		return new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	}

	public static int[] getCubeIndices() {
		return new int[] { 
				0, 1, 3, 
				3, 1, 2, 
				4, 5, 7, 
				7, 5, 6, 
				8, 9, 11,
				11, 9, 10, 
				12, 13, 15, 
				15, 13, 14, 
				16, 17, 19, 
				19, 17, 18, 
				20, 21, 23, 
				23, 21, 22 };
	}
	
	public void updateVBO(int vboID, float[] data, FloatBuffer buffer) {
		buffer.clear();
		buffer.put(data);
		buffer.flip();
		// Bind VBO.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		// Reallocate memory to the VBO.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity() * 4, GL15.GL_STREAM_DRAW);
		// Store new data.
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
		// Unbind VBO.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public static ModelData createCube(float radius) {
		return new ModelData(getCubeVertices(radius), getCubeUVs(), getCubeMapNormals(), getCubeIndices(), 0);
	}
}
