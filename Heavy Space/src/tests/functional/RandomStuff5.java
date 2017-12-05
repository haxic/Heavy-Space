package tests.functional;

import org.joml.Vector3f;

public class RandomStuff5 {

	public static void main(String[] args) {
		Vector3f velocity1 = new Vector3f( 0,1, -1).normalize(); // forward
		Vector3f velocity2 = new Vector3f(0, 1, 1).normalize(); // up
		
		Vector3f position1 = new Vector3f(0, 0, 0);
		Vector3f position2 = new Vector3f(10, 0, 0);
		
		System.out.println(velocity1.cross(velocity2, new Vector3f()));
	}

}
