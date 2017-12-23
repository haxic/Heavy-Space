package tests.functional;

import org.joml.Vector3f;

import shared.components.ObjectComponent;

public class RandomStuff5 {

	public static void main(String[] args) {
		ObjectComponent o1 = new ObjectComponent(new Vector3f());
		o1.rotate(20, 0, 0);
		o1.rotate(0, 10, 0);
		System.out.println(o1.getOrientation().x + " " + o1.getOrientation().y + " " + o1.getOrientation().z + " " + o1.getOrientation().w);
		System.out.println(o1.getForward().x + " " + o1.getForward().y + " " + o1.getForward().z);
		
		System.out.println("o2");
		ObjectComponent o2 = new ObjectComponent(new Vector3f());
		o2.getOrientation().set(o1.getOrientation().x, o1.getOrientation().y, o1.getOrientation().z);
		o2.updateOrientation();
		System.out.println(o2.getOrientation().x + " " + o2.getOrientation().y + " " + o2.getOrientation().z + " " + o2.getOrientation().w);
		System.out.println(o2.getForward().x + " " + o2.getForward().y + " " + o2.getForward().z);
	}

}
