Craig Glassbrenner
Decision Tree
10/05/2020

README:

This program is my implementation of the Decision Tree learning algorithm. The data set that I used to create my decision tree is the mushroom data provided in the file "mushroom_data.txt". The goal of this program is to create a decision tree based on the first 22 traits of the mushroom in order to accurately predict the last trait which is whether the mushroom in question is edible or poisonous. 
How to Compile this program:

This program was constructed in java so from the command line you will want to do:
javac Driver.java
** This will compile the program **

java Driver
** This will run the program with TrainSize = 10, MaxSize = 100, numTrials = 20 **

If you are looking to change those parameters then when you run the program using "java Driver" after you can include the command line arguments as follows:

-i x -- Where x is some integer, this will change the size of the training set.
-l x -- Where x is some integer, this will change the max size of the training set.
-t x -- Where x is some integer, this will change the number of trails run each time. 
