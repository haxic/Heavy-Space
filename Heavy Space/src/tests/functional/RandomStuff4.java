package tests.functional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RandomStuff4 {

	public static void main(String[] args) {
		List<Integer> ints = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			ints.add((int) (Math.random()*1000));
		}
		for (Iterator<Integer> iterator = ints.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
			iterator.remove();
			ints.remove(integer);
			System.out.println(integer);
			for (Integer integer2 : ints) {
				System.out.println("    " + integer2);
			}
		}
	}

}
