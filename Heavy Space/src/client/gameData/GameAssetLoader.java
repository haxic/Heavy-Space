package client.gameData;

import org.joml.Vector3f;

import client.models.Model;
import client.models.ModelAttachmentPoint;
import client.models.Texture;
import utilities.Loader;
import utilities.OBJFileLoader;

public class GameAssetLoader {
	public Model dragon;
	public Model stall;
	public Model fern;
	public Texture particleAtlasTexture;
	public Model skybox;
	public Model cube;
	public Model sphere;
	public Model iceAsteroidSmall;
	public Model iceAsteroid;
	public Model ship;
	public Model cannonProjectile;

	public GameAssetLoader(Loader loader) {
		dragon = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("dragon")), loader.loadTexture("dragon", 1, 1));
		
		dragon.putAttachmentPoint(ModelAttachementTag.FRONT, new ModelAttachmentPoint(new Vector3f(10, 0, 0), new Vector3f(0, 0, 0)));
		stall = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("stall")), loader.loadTexture("stallTexture", 1, 1));
		fern = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("fern")), loader.loadTexture("fern", 2, 1));
		fern.setHasTransparency(true);
		fern.setAllowBackLighting(true);
		particleAtlasTexture = loader.loadTexture("cosmic", 4, 1);
		skybox = loader.loadSkybox("space", 10000);
		
		cube = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("cube")), loader.loadTexture("texture", 1, 1));
		sphere = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("sphere")), loader.loadTexture("texture", 1, 1));
	
		
		iceAsteroidSmall = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("asteroids/iceSmall/model")), loader.loadTexture("asteroids/iceSmall/texture", 1, 1));
		iceAsteroid = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("asteroids/ice/ice")), loader.loadTexture("asteroids/ice/iceTexture", 1, 1));
		ship = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("ship/ApacheClass")), loader.loadTexture("ship/ApacheClassTexture", 1, 1));
		cannonProjectile = new Model(loader.loadToVAO(OBJFileLoader.loadOBJ("cube")), loader.loadTexture("yellow", 1, 1));
	}
}
