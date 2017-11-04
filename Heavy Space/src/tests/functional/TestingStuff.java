package tests.functional;

import java.util.ArrayList;
import java.util.List;

import client.entities.Particle;
import utilities.InsertionSort;

public class TestingStuff {
	public static void main(String[] args) {
		// Full sort ArrayList
		List<Particle> particles = new ArrayList<Particle>();
		for (int a = 0; a < 50; a++) {
			Long start = System.currentTimeMillis();
			int numbers = (int) (Math.random() * 100 + 20);
			for (int i = 0; i < numbers; i++) {
//				particles.add(new Particle2((float) (Math.random() * 10000 - 5000)));
			}
			InsertionSort.sortHighToLow(particles);
			System.out.println("Added " + numbers + ". " + particles.size() + " total sorted in + " + (System.currentTimeMillis() - start));
		}
		System.out.println(1000 / 144);
	}
}
