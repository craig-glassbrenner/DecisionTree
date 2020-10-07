/*
 * Craig Glassbrenner
 * 10/05/2020
 * 
 * This does all the hard work for the decision tree, it creates the train array and test array, reads in all the mushrooms from the text file
 * and saves it into an array of mushrooms. Build the decision tree with the training set and tests the decision tree with the test set. 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BuildTree {
	
	int trainSize;
	int largestSize;
	int numTrials;
	
	List<Mushroom> arr;
	Node root;
	
	double trainAcc;
	double testAcc;
	
	public BuildTree() {}
	
	// This method reads in the mushroom data from the text file and controls out output (summary statistics)
	public void setup(int train, int ls, int nt) throws IOException {
		trainSize = train;
		largestSize = ls;
		numTrials = nt;
		arr = new ArrayList<>();
		
		File file = new File("./mushroom_data.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		int d = 0;
		while((line = br.readLine()) != null) {
			buildArray(line, d);
			d++;
		}
		
		br.close();
		
		System.out.printf("Results averaged across %d trials\n", numTrials);
		System.out.println("TrainSize     TrainAcc     TestAcc");
		
		int k=1;
		while((trainSize*k) <= largestSize) {
			trainAcc = 0;
			testAcc = 0;
			
			for(int i=0; i < numTrials; i++) {
				makeSets(trainSize*k);
			}
			
			trainAcc = trainAcc / (double) numTrials;
			testAcc = testAcc / (double) numTrials;
			
			System.out.printf("%d       %f        %f\n", trainSize*k, trainAcc, testAcc);
			k++;
		}
	}
	
	// Creates individual mushrooms from each line in the text file and saves this data to the mushroom array.
	public void buildArray(String line, int index) {
		String[] values = line.split(" ");
		boolean d;
		
		if(values[values.length - 1].equals("p")) {
			d = true;
		} else {
			d = false;
		}
		
		Mushroom m = new Mushroom(values, d, index);
		arr.add(m);
	}
	
	// Creates the training set and test set, keeps track of the accuracy of each set when we test 
	// in order to print this out to stdout. 
	public void makeSets(int size) {
		Random rand = new Random();
		List<Mushroom> trainingSet = new ArrayList<>();
		List<Mushroom> testSet = new ArrayList<>();
		
		for(int i=0; i < arr.size(); i++) {
			testSet.add(arr.get(i));
		}
		
		// randomly selects certain mushrooms and adds these to training set/removes from test set. 
		for(int i=0; i < size; i++) {
			int toAdd = rand.nextInt(arr.size());
			trainingSet.add(arr.get(toAdd));
			
			for(int k=0; k < testSet.size(); k++) {
				if(testSet.get(k).attVal == toAdd) {
					testSet.remove(k);
				}
			}
		}
		
		// List of integers that represent the attribute numbers
		List<Integer> indices = new ArrayList<>();
		for(int j=0; j < 22; j++) {
			indices.add(j);
		}
		
		// Start of the tree
		root = new Node();
		buildTree(trainingSet, indices, root);
		
		double acc_test = test(testSet);
		double acc_train = test(trainingSet);
		
		trainAcc = trainAcc + acc_train;
		testAcc = testAcc + acc_test;
		
	}
	
	// Calculates the accuracy of the test set
	public double test(List<Mushroom> test) {
		int numCorrect = 0;
		
		for(int i=0; i < test.size(); i++) {
			Mushroom e = test.get(i);
			boolean correct = testInd(e);
			
			if(correct) {
				numCorrect++;
			}
		}
		double acc = numCorrect / (double) test.size();
		return acc;
	}
	
	// Takes in one mushroom value and tests the tree, returns if the value guessed by the tree is correct or incorrect.
	public boolean testInd(Mushroom m) {
		boolean toReturn;
		Node cur = root;
		
		// Once splitIndex == -1 we know we have reached a leaf node
		while(cur.splitIndex != -1) {
			String[] s = getString(cur.splitIndex);
			String val = m.s[cur.splitIndex];
			
			// Finds which child node has same value as the mushroom being tested so we can move to that node
			for(int i=0; i < s.length; i++) {
				if(s[i].equals(val)) {
					cur = cur.children[i];
					break;
				}
			}
		}
		
		if((cur.cls).equals("p") && m.deadly) {
			toReturn = true;
		} else if((cur.cls).equals("p") && !m.deadly) {
			toReturn = false;
		} else if((cur.cls).equals("e") && !m.deadly) {
			toReturn = true;
		} else {
			toReturn = false;
		}
		
		return toReturn;
	}
	
	// This builds the decision tree based on the training set provided from makeSets
	public Node buildTree(List<Mushroom> train, List<Integer> i, Node mostRecent) {
		// If training group is empty or we don't have anymore attributes in the integer list, returns classification of parent node
		if(train.isEmpty() || i.isEmpty()) {
			return mostRecent;
			
		// If all mushrooms in training set have the same poisonous/edible value then return that value for classification
		} else if(allSame(train)) {
			String s;
			if(train.get(0).deadly) {
				s = "p";
			} else {
				s = "e";
			}
			mostRecent.cls = s;
			return mostRecent;
			
		// This is where the hard work is done for the decision tree
		} else {
			
			// Calculates which attribute has the highest importance (most info gained from splitting on this attribute)
			double maxEntropy = importance(i.get(0), train);
			int att = i.get(0);
			for(int j=1; j < i.size(); j++) {
				double e = importance(i.get(j), train);
				
				if(e > maxEntropy) {
					maxEntropy = e;
					att = i.get(j);
				}
			}
			
			// Gets String array of children options
			String[] options = getString(att);
			// Sets nodes splitIndex to whatever integer attribute we are splitting on (decided by maxEntropy)
			mostRecent.splitIndex = att;
			// Instantiates children array for this node to the same size as options declared above
			mostRecent.children = new Node[options.length];
			
			int numPois = 0;
			int numEd = 0;
			for(int j=0; j < train.size(); j++) {
				if(train.get(j).deadly) {
					numPois++;
				} else {
					numEd++;
				}
			}
			
			// Classifies node with poisonous or edible, whichever has more values in the training set
			if(numPois >= numEd) {
				mostRecent.cls = "p";
			} else {
				mostRecent.cls = "e";
			}
			
			// For each child node
			for(int j=0; j < options.length; j++) {
				List<Mushroom> l = new ArrayList<>();
				List<Integer> attributeList = i;
				
				// Removes attribute from list of attribute (already split on it and don't want to do it again)
				for(int c=0; c < attributeList.size(); c++) {
					if(attributeList.get(c) == att) {
						attributeList.remove(c);
					}
				}
				
				Node child = new Node();
				child.cls = mostRecent.cls;
				mostRecent.children[j] = child;
				
				// Splits training set based on attribute value that we are splitting on so all types are grouped seperately
				for(int k=0; k < train.size(); k++) {
					if(train.get(k).s[att].equals(options[j])) {
						l.add(train.get(k));
					}
				}
				
				// Recursively calls buildTree on the child node until so we can get to a leaf node
				buildTree(l, attributeList, child);
			}
			return mostRecent;
		}
	}

	// Calculates the importance of the attribute provided
	public double importance(int att, List<Mushroom> train) {
		double entropy = calcEntropy(train);
		
		String[] numOptions = getString(att);
		Integer[] count = new Integer[numOptions.length];
		double total = 0;
		for(int i=0; i < numOptions.length; i++) {
			count[i] = 0;
		}
		
		for(int j=0; j < train.size(); j++) {
			for(int k=0; k < numOptions.length; k++) {
				if(numOptions[k].equals(train.get(j).s[att])) {
					count[k]++;
					total++;
				}
			}
		}
		
		// Calculates remainder
		double remainder = 0;
		for(int i=0; i < count.length; i++) {
			List<Mushroom> l = new ArrayList<>();
			for(int j=0; j < train.size(); j++) {
				if(numOptions[i].equals(train.get(j).s[att])) {
					l.add(train.get(j));
				}
			}
			
			double e = calcEntropy(l);
			remainder = remainder + ((count[i] / total) * e);
		}
		
		// Returns information gained
		double gain = entropy - remainder;
		return gain;
	}
	
	// Calculates and returns entropy
	public double calcEntropy(List<Mushroom> l) {
		double pos = 0;
		double neg = 0;
		
		for(int i=0; i < l.size(); i++) {
			if(l.get(i).deadly == true) {
				pos++;
			} else {
				neg ++;
			}
		}
		
		double ent;
		
		if(pos == 0 || neg == 0) {
			ent = 0;
		} else {
			ent = -1*(pos / (pos + neg))* (Math.log(pos / (pos + neg)) / Math.log(2));
			ent = ent - (neg / (pos + neg))* (Math.log(neg / (pos + neg)) / Math.log(2));
		}
		
		return ent;
	}
	
	// Returns an array of Strings that are just the attribute options for each given attribute
	public String[] getString(int a) {
		if(a == 0) {
			String[] toReturn = {"b", "c", "x", "f", "k", "s"};
			return toReturn;
		} else if(a == 1) {
			String[] toReturn = {"f", "g", "y", "s"};
			return toReturn;
		} else if(a == 2) {
			String[] toReturn = {"n", "b", "c", "g", "r", "p", "u", "e", "w", "y"};
			return toReturn;
		} else if(a == 3) {
			String[] toReturn = {"t", "f"};
			return toReturn;
		} else if(a == 4) {
			String[] toReturn = {"a", "l", "c", "y", "f", "m", "n", "p", "s"};
			return toReturn;
		} else if(a == 5) {
			String[] toReturn = {"a", "d", "f", "n"};
			return toReturn;
		} else if(a == 6) {
			String[] toReturn = {"c", "w", "d"};
			return toReturn;
		} else if(a == 7) {
			String[] toReturn = {"b", "n"};
			return toReturn;
		} else if(a == 8) {
			String[] toReturn = {"k", "n", "b", "h", "g", "r", "o", "p", "u", "e", "w", "y"};
			return toReturn;
		} else if(a == 9) {
			String[] toReturn = {"e", "t"};
			return toReturn;
		} else if(a == 10) {
			String[] toReturn = {"b", "c", "u", "e", "z", "r"};
			return toReturn;
		} else if(a == 11) {
			String[] toReturn = {"f", "y", "k", "s"};
			return toReturn;
		} else if(a == 12) {
			String[] toReturn = {"f", "y", "k", "s"};
			return toReturn;
		} else if(a == 13) {
			String[] toReturn = {"n", "b", "c", "g", "o", "p", "e", "w", "y"};
			return toReturn;
		} else if(a == 14) {
			String[] toReturn = {"n", "b", "c", "g", "o", "p", "e", "w", "y"};
			return toReturn;
		} else if(a == 15) {
			String[] toReturn = {"p", "u"};
			return toReturn;
		} else if(a == 16) {
			String[] toReturn = {"n", "o", "w", "y"};
			return toReturn;
		} else if(a == 17) {
			String[] toReturn = {"n", "o", "t"};
			return toReturn;
		} else if(a == 18) {
			String[] toReturn = {"c", "e", "f", "l", "n", "p", "s", "z"};
			return toReturn;
		} else if(a == 19) {
			String[] toReturn = {"k", "n", "b", "h", "r", "o", "u", "w", "y"};
			return toReturn;
		} else if(a == 20) {
			String[] toReturn = {"a", "c", "n", "s", "v", "y"};
			return toReturn;
		} else {
			String[] toReturn = {"g", "l", "m", "p", "u", "w", "d"};
			return toReturn;
		}
	}
	
	// Returns true if Mushroom list has all the same poisonous or edible value
	public boolean allSame(List<Mushroom> l) {
		boolean firstVal = l.get(0).deadly;
		
		for(int i=0; i < l.size(); i++) {
			if(l.get(i).deadly != firstVal) {
				return false;
			}
		}
		
		return true;
	}
	
	// Node class to build the decision tree with
	// Children - array of children nodes
	// splitIndex - what index value this node spilt on
	// cls - classification of Node (poisonous or edible)
	public class Node {
		Node[] children;
		int splitIndex;
		String cls;
		
		public Node() {
			children = null;
			splitIndex = -1;
			cls = null;
		}
	}
}

