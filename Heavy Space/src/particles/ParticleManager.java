package particles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Matrix4f;

import entities.Camera;
import models.Texture;
import renderers.ParticleRenderer;
import utilities.InsertionSort;
import utilities.Loader;

public class ParticleManager {
	private static Map<Texture, List<Particle>> particles = new HashMap<>();
	private static ParticleRenderer particleRenderer;

	public static void init(Loader loader) {
		particleRenderer = new ParticleRenderer(loader);
	}

	public static void update(Camera camera, float delta) {
		Iterator<Entry<Texture, List<Particle>>> mapIterator = particles.entrySet().iterator();
		while (mapIterator.hasNext()) {
			List<Particle> list = mapIterator.next().getValue();
			Iterator<Particle> iterator = list.iterator();
			while (iterator.hasNext()) {
				Particle particle = iterator.next();
				if (!particle.update(camera, delta)) {
					iterator.remove();
					if (list.isEmpty())
						mapIterator.remove();
				}
			}
			InsertionSort.sortHighToLow(list);
		}
	}

	public static void renderParticles(Camera camera) {
		particleRenderer.render(particles, camera);
	}

	public static void cleanUp() {
		particleRenderer.cleanUp();
	}

	public static void addParticle(Particle particle) {
		List<Particle> list = particles.get(particle.getTexture());
		if (list == null)
			particles.put(particle.getTexture(), list = new ArrayList<Particle>());
		list.add(particle);
	}
}
