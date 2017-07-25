package view.util;

import java.util.ArrayList;

public class Util {
	
	public static void print(Object o) {
		System.out.print(o);
	}

	public static void println(Object o) {
		if (null == o)
			System.out.println();
		else
			System.out.println(o);
	}

	public static ArrayList<Integer> toArrayList(int[] ints) {
		if (ints.length == 0)
			return null;
		ArrayList<Integer> al = new ArrayList<Integer>();
		for (int i = 0; i < ints.length; i++) {
			al.add(ints[i]);
		}
		return al;
	}
}