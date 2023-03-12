# You may have to edit this file to delete header lines produced by
# mailers or news systems from this file (all lines before these shell
# comments you are currently reading).

# Shell archive made by dwjones on Tue 13 Apr 2021 10:32:26 AM CDT

# To install this software on a UNIX system:
#  1) create a directory (e.g. with the shell command mkdir stuff)
#  2) change to that directory (e.g. with the command cd stuff),
#  3) direct the remainder of this text to sh (e.g. sh < ../savedmail).
# This will make sh create files in the new directory; it will do
# nothing else (if you're paranoid, you should scan the following text
# to verify this before you follow these directions).  Then read README
# in the new directory for additional instructions.







xxxxxxxxxx
cat > README <<\xxxxxxxxxx
EPIDEMIC SIMULATOR
==================

Author:  Douglas W. Jones
Version: Apr. 13, 2021

The code in this directory includes a solution to Machine Problem 10 from
CS:2820 at the University of Iowa.

This epidemic simulator takes an input file containing a description of the
places in a community, the roles fulfilled by the population of that community
and the nature of the disease.  The output is a CSV file showing the progress
of the disease through the community.

Files
-----

This directory contains the following source files for the epidemic simulator:

* Error.java		error reporting framework
* MyScanner.java	Wrapper around java.util.scanner
* Check.java		Utility to do sanity checks on values
* MyRandom.java		Extensions to Java.util.random
* Simulator.java	Simulation framework
* Time.java		Definitions of time units

* InfectionRule.java	How do stages of the infection progress
* Schedule.java		How do people decide to move from place to place
* Person.java		How does each person behave, also population statistics
* Place.java		How does each place work
* PlaceKind.java	What kinds of places are there
* Role.java		What kinds of roles to people fit into

* Epidemic.java		the main program

The following additional files are included

* README		this file
* testa			test input, workers spread disease between families
* testb			test input, everyone works sometimes, spreading it
* testc			test input, two compartment, everyone has brief contact
* testd			test input, two compartment, fewer extended contacts

Instructions
------------

To build the epidemic simulator, use this shell command

	javac Epidemic.java

To test or demonstrate the simulator, use one of these shell commands

	java Epidemic testa
	java Epidemic testb
	java Epidemic testc
	java Epidemic testd

Tests A and B should produce very similar results as a wave of infection
sweeps through the community until everyone has either recovered or died of
the simulated disease.

Tests C and D are bi-stable; they involve places named earth and mars, where
people from those planets travel to the moon and make brief contact.
Sometimes, the epidemic fails to spread between planets, sometimes, it
jumps the gap between planets and sweeps through both.

Tests A and C involve people following fixed schedules, while tests B and D
involve schedules with random elements, where the random elements have been
adjusted so that test B produces output similar to test A and D similar to C.
xxxxxxxxxx
cat > testa <<\xxxxxxxxxx
population 100;                     latent       2.0 0;
infected 1;                         asymptomatic 2   0;
place home  10  0 0.01;             symptomatic  2   0   0.9;
place work  10  0 0.01;             bedridden    2   0   0.9;
role homebody 60 home;
role worker   40 home work (9-17);
end 30;
xxxxxxxxxx
cat > testb <<\xxxxxxxxxx
population 100;                     latent       2.0 0;
infected 1;                         asymptomatic 2   0;
place home  10  0 0.01;             symptomatic  2   0   0.9;
place work  10  0 0.01;             bedridden    2   0   0.9;
role everybody 100 home work (9-17 0.4);
end 30;
xxxxxxxxxx
cat > testc <<\xxxxxxxxxx
population 100;               latent       2.0 0;
infected 1;                   asymptomatic 3   0;
place earth 100 0 0.001;      symptomatic  5   1   0.9;
place moon  100 0 .0001;      bedridden    8   2   0.9;
place mars  100 0 0.001;
role human   50 earth  moon (10-11.06);
role martian 50 mars   moon (11-12);
end 30;
xxxxxxxxxx
cat > testd <<\xxxxxxxxxx
population 100;               latent       2.0 0;
infected 1;                   asymptomatic 3   0;
place earth 100 0 0.001;      symptomatic  5   1   0.9;
place moon  100 0 .0001;      bedridden    8   2   0.9;
place mars  100 0 0.001;
role human   50 earth  moon (11-12 0.1);
role martian 50 mars   moon (11-12 0.1);
end 30;
xxxxxxxxxx
