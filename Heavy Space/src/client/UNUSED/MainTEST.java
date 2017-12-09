package client.UNUSED;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import client.components.ActorComponent;
import client.display.DisplayManager;
import client.entities.Camera;
import client.entities.LightComponent;
import client.gameData.GameModelLoader;
import client.gameData.ParticleComponent;
import client.inputs.KeyboardHandler;
import client.models.Model;
import client.renderers.RenderManager;
import shared.functionality.DataPacket;
import shared.functionality.network.TCPSocket;
import shared.functionality.network.UDPServer;
import shared.game.Entity;
import tests.LocalConfig;
import utilities.Loader;

public class MainTEST {

	public static void main(String[] args) {
		Loader loader = new Loader();
		DisplayManager displayManager = new DisplayManager(1200, 800);
		Model skybox = loader.loadSkybox("space", 500);
		GameModelLoader gameModelLoader = new GameModelLoader(loader);
		RenderManager renderManager = new RenderManager(displayManager, skybox, loader, gameModelLoader.particleAtlasTexture);
		Camera camera = new Camera();

		ParticleComponent plasmaParticleSystem = new ParticleComponent(gameModelLoader.particleAtlasTexture, new Vector3f(10, 0, -10), 0, 0.01f, 1);
		renderManager.addParticleEntity(plasmaParticleSystem);
		renderManager.addParticleEntity(new ParticleComponent(gameModelLoader.particleAtlasTexture, new Vector3f(20, 0, -10), 1, 0.1f, 3));
		List<LightComponent> lights = new ArrayList<LightComponent>();

		LightComponent sun = new LightComponent(new Vector3f(0, 1000, 10000), new Vector3f(1, 1, 0), new Vector3f(0, 0, 0));

		LightComponent plasma1 = new LightComponent(new Vector3f(10, 0, -10), new Vector3f(0, 1f, 1f), new Vector3f(0.01f, 1, 1));
		LightComponent plasma2 = new LightComponent(new Vector3f(20, 0, -10), new Vector3f(0, 1f, 1f), new Vector3f(0.01f, 1, 1));
		lights.add(sun);
		lights.add(plasma1);
		lights.add(plasma2);

		LocalConfig config = new LocalConfig();
		TCPSocket tcp = null;
		try {
			tcp = new TCPSocket(new Socket("localhost", config.gameServerDefaultPort));
			tcp.sendData("hello world".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		UDPServer udp = new UDPServer("localhost", config.gameClientDefaultPort);
		InetAddress address = null;
		try {
			udp.startServer();
			address = InetAddress.getByName("localhost");
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}

		List<ActorComponent> actors = new ArrayList<ActorComponent>();
		// actors.add(new Actor(new Entity(new Vector3f(0, 20, 0), new
		// Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f)),
		// gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(0, 0, -25), new
		// Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(0, 0, 25), new
		// Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(25, 0, 0), new
		// Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(-25, 0, 0), new
		// Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));
		// actors.add(new Actor(new Entity(new Vector3f(-25, 0, 0), new
		// Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.stall));

		ActorComponent dragonActor = new ActorComponent(new Entity(new Vector3f(0, 0, -20), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.dragon);
		actors.add(dragonActor);
		ActorComponent fernActor = new ActorComponent(new Entity(new Vector3f(10, 0, -15), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.fern);
		actors.add(fernActor);
		ActorComponent fernActor2 = new ActorComponent(new Entity(new Vector3f(-10, 0, -15), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), gameModelLoader.fern, 3);
		actors.add(fernActor2);

		for (ActorComponent actor : actors) {
			renderManager.addActor(actor);
		}

		long currentTimeMillis;
		long timer = System.currentTimeMillis();
		// long removeTimer = timer;
		// int removeId = 0;
		int frames = 0;
		float mouseSpeed = 0.25f;
		float speed = 25f;
		float rollSpeed = 2f;
		int invertX = 1;
		int invertY = -1;

		Vector3f position = new Vector3f();
		DataPacket dataPacket;
		byte[] data;
		while (!displayManager.shouldClose()) {
			while ((data = udp.getData()) != null) {
				// Pretend that this is the message reader
				dataPacket = new DataPacket(data);
				Vector3f newPosition = new Vector3f(dataPacket.getInteger() / 1000.0f, dataPacket.getInteger() / 1000.0f, dataPacket.getInteger() / 1000.0f);
				camera.position = newPosition;
			}
			position.set(camera.position);
			// Handle inputs

			displayManager.pollInputs();
			float dt = displayManager.getDeltaTime();
			currentTimeMillis = System.currentTimeMillis();
			float hor = invertX * mouseSpeed * dt * (float) (displayManager.getWidth() / 2 - displayManager.getMouseX());
			float ver = invertY * mouseSpeed * dt * (float) (displayManager.getHeight() / 2 - displayManager.getMouseY());
			if (!displayManager.isCursorEnabled()) {
				camera.pitch(ver);
				camera.yaw(hor);
			}
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_W))
				position.add(camera.getForward().mul(dt * speed, new Vector3f()));
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_S))
				position.sub(camera.getForward().mul(dt * speed, new Vector3f()));
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_D))
				position.add(camera.right.mul(dt * speed, new Vector3f()));
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_A))
				position.sub(camera.right.mul(dt * speed, new Vector3f()));

			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_Q))
				camera.roll(dt * rollSpeed);
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_E))
				camera.roll(-dt * rollSpeed);

			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
				position.add(camera.up.mul(dt * speed, new Vector3f()));
			if (KeyboardHandler.kb_keyDown(GLFW.GLFW_KEY_LEFT_SHIFT))
				position.sub(camera.up.mul(dt * speed, new Vector3f()));

			if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_LEFT_ALT))
				displayManager.toggleCursor();
			if (KeyboardHandler.kb_keyDownOnce(GLFW.GLFW_KEY_Z))
				camera.toggleLockUp();
			dataPacket = new DataPacket(new byte[200]);
			dataPacket.addInteger((int) (position.x * 1000));
			dataPacket.addInteger((int) (position.y * 1000));
			dataPacket.addInteger((int) (position.z * 1000));
			dataPacket.addByte((byte) 20);
			DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getData(), dataPacket.size(), address, config.gameServerDefaultPort);
			udp.sendData(datagramPacket);
			// Logic
			float bounceFactor = (float) Math.cos(((currentTimeMillis % 6100.0) / 1000.0));
			if (bounceFactor < 0)
				bounceFactor = -bounceFactor;
			// dragonActor.getEntity().getRotation().y += 0.5f;
			// fernActor.getEntity().getRotation().y += 0.5f;
			plasma1.getLinearThrust().set(0, bounceFactor * 20, -20);
			plasmaParticleSystem.setPosition(plasma1.getLinearThrust());

			renderManager.update(camera, dt);

			// Render scene
			renderManager.render(camera, lights);

			// Update display (draw on display)s
			displayManager.updateDisplay();

			// CHECK FPS
			frames++;
			if (System.currentTimeMillis() - timer >= 1000) {
				timer += 1000;
				frames = 0;
			}
			// REMOVE ENTITIES PERIODICALLY - for testing purposes
			// if (System.currentTimeMillis() - removeTimer >= 5000 && removeId
			// < actors.size()) {
			// renderManager.removeActor(actors.get(removeId++));
			// removeTimer += 2000;
			// }
		}

		renderManager.cleanUp();
		displayManager.closeDisplay();
		loader.cleanUp();
		udp.requestClose();
		try {
			tcp.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
