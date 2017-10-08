package utilities;

import java.nio.ByteBuffer;

public class TextureData {
	public int width;
	public int height;
	public ByteBuffer buffer;

	public TextureData(int width, int height, ByteBuffer buffer) {
		this.width = width;
		this.height = height;
		this.buffer = buffer;
	}

}
