package gameData;

import org.joml.Vector3f;

import models.Model;
import models.ModelAttachmentPoint;
import utilities.Loader;
import utilities.OBJFileLoader;

public class GameModelLoader {
	public Model dragon;
	public Model stall;
	public Model fern;

	public GameModelLoader(Loader loader) {
		dragon = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("dragon")), loader.loadTexture("dragon", 1, 1));
		dragon.putAttachmentPoint(ModelAttachementTag.FRONT, new ModelAttachmentPoint(new Vector3f(10, 0, 0), new Vector3f(0, 0, 0)));
		stall = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("stall")), loader.loadTexture("stallTexture", 1, 1));
		fern = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("fern")), loader.loadTexture("fern", 2, 1));
		fern.setHasTransparency(true);
		fern.setAllowBackLighting(true);
	}
}
