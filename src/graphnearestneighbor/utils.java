/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

import java.util.Random;

/**
 * auxiliary stuff
 * @author Nil
 */
class utils {
	
	private static Random R;

	//returns pseudorandom int between min and max, both included
	static int randInt(int min, int max) {
		if (R == null) R = new Random(System.currentTimeMillis());
		return min + R.nextInt(max-min+1);
	}
	
	//basic class to group 2 objects
	static class Pair<V1,V2> {
		V1 first;
		V2 second;
		Pair(V1 first, V2 second) {
			this.first = first;
			this.second = second;
		}
	}
	
	//converts a double to a string nicely for humans
	static String prettyStr(double d, int decimalPlaces) {
		if (d == Double.MAX_VALUE) return "INF";
		String s = String.valueOf(d);
		if (!s.contains(".")) return s;
		String sInt = s.substring(0, s.indexOf('.'));
		String sFrac = s.substring(s.indexOf('.')+1);
		boolean fracIsZero = true;
		for (int i = 0; i < sFrac.length(); i++) {
			if (sFrac.charAt(i) != '0') fracIsZero = false;
		}
		if (fracIsZero) return sInt;
		sFrac = sFrac.substring(0, Math.min(sFrac.length(), decimalPlaces));
		return sInt+'.'+sFrac;
	}
	
}
