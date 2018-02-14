# DataInsight
Code solution for the Data Insight Data Engineering Problem.

Used Java to solve a challenge problem for Data Insight.
The following is the approach used:

1. Extracted data from FEC dataset on politicians given in the form of .txt file.
2. Extracted only the columns required and stored them.
3. Used the stored data as a stream and checked for repeat donors.
4. When found, search for same recipients from same area and year.
5. Calculated the running percentile from the list and total contributions of the repeating donors.
6. Wrote the data into a text file repeat_donors.txt with the required information.

The approach taken is very simple and straight forward. Java 7 has been used for all computations. No extra dependencies were required.
ArrayLists, HashMaps and Collections, all classes found in the utils package of Java were used.
Input required is the filename - both the percentile.txt and itcont.txt. Output file generated is repeat_donors.txt.
Directory Structure followed as per instructions.

The code in run.sh first compiles the classes in src and stores them in bin folder. Then it runs the code from the bin folder files using the required filenames - input/percentile.txt input/itcont.txt and output/repeat_donors.txt.
Please delete the repeat_donor.txt file in output folder before running run.sh. You may / may not wish to delete the .class files in bin. It will be compiled anyway. The tests are shown to run perfectly via Windows 10. There might be some \n (new line) issues on Linux which I faced on my machine.

Note: I was afforded 1 additional day to submit (at 9am PST on 14th February, 2018). This was due to my committments as a full time graduate student at University of Southern California.
