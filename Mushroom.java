/*
 * Craig Glassbrenner
 * 10/05/2020
 * 
 * Mushroom Class, contains information on whether or not the mushroom is poisonous or edible
 * All the attribute values read in from the file are stored in s
 * attVal - is the integer that indicates where this mushroom was read in, helps us when we need to remove 
 * from testSet and add to trainingSet
 */

public class Mushroom {
	boolean deadly;
	String[] s = new String[22];
	int attVal;
	
	public Mushroom(String[] val, boolean d, int a) {
		s = val;
		deadly = d;
		attVal = a;
	}
}
