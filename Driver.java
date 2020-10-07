/*
 * Craig Glassbrenner
 * 10/05/2020
 * Decision Tree Implementation
 * 
 * This program is my implementation of the Decision Tree learning algorithm. 
 * The data set that I used to create my decision tree is the mushroom data provided in the file "mushroom_data.txt". 
 * The goal of this program is to create a decision tree based on the first 22 traits of the mushroom in order to 
 * accurately predict the last trait which is whether the mushroom in question is edible or poisonous. 
 * 
 */
import java.io.IOException;

// This will parse command line arguments and end the program gracefully if the arguments entered are unaccepted.

public class Driver {

	public static void main(String[] args) throws IOException {
		int trainSize = 10;
		int largestSize = 100;
		int numTrials = 20;
		
		BuildTree b = new BuildTree();
		
		if(args.length == 0) {
			b.setup(trainSize, largestSize, numTrials);
		} else if(args.length > 6) {
			System.out.println("Too many command line arguments.");
			System.exit(0);
		} else {
			
			for(int i=0; i < args.length; i++) {
				
				if(args[i].equals("-i")) {
					try {
						trainSize = Integer.parseInt(args[i+1]);
						i++;
					} catch(Exception e) {
						System.out.println("Did not follow -i with an integer.");
						System.exit(0);
					}
				} else if(args[i].equals("-l")) {
					try {
						largestSize = Integer.parseInt(args[i+1]);
						i++;
					} catch (Exception e) {
						System.out.println("Did not follow -l with an integer.");
						System.exit(0);
					}
				} else if(args[i].equals("-t")) {
					try {
						numTrials = Integer.parseInt(args[i+1]);
						i++;
					} catch(Exception e) {
						System.out.println("Did not follow -t with an integer.");
						System.exit(0);
					}
				} else {
					System.out.println("This is not a valid command line argument.");
					System.exit(0);
				}
			}
			
			b.setup(trainSize, largestSize, numTrials);
		}

	}

}
