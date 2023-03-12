# Makefile for the Epidemic Simulator
# Author: Zhongxiang Wang
# Version: Apr. 19, 2021

# Support for:
#   make                    -- make the default target
#   make Epidemic.class  -- the default target

# Plus the following utilities
#   make demo               -- demonstrate the road network simulator
#   make clean              -- delete all files created by make
#   make html               -- make javadoc web site from simulat

##########
# all source files

supportJava = Error.java MyScanner.java MyRandom.java Simulator.java \
	Time.java Check.java Schedule.java

peopleJava = Person.java InfectionRule.java Role.java
placesJava = Place.java PlaceKind.java
modelJava = $(peopleJava) $(placesJava)

mainJava = Epidemic.java

allJava = $(supportJava) $(modelJava) $(mainJava)

########
# Layer 1: (default make target) the main program
.PHONY: clean

Epidemic.class: Error.class MyScanner.class Check.class MyRandom.class \
				Simulator.class Time.class InfectionRule.class \
				Schedule.class Person.class PlaceKind.class \
				Place.class Role.class
	javac Epidemic.java

########
# Layer 2: core of the model used to build and simulate the road network
#    Note:  There is a dependency knot tying the core classes together.
#     Dependencies in the knot are listed explicitly for documentation purposes.
#     The make utility will flag these when make is used, they do no harm to it.

InfectionRule.class: MyRandom.class
	javac InfectionRule.java

Person.class: InfectionRule.class MyScanner.class \
				Role.class Place.class Person.class \
				Simulator.class
	javac Person.java

PlaceKind.class: Schedule.class Place.class Person.class MyRandom.class
	javac PlaceKind.java

Place.class: PlaceKind.class Person.class
	javac Place.java

Role.class: MyScanner.class PlaceKind.class Error.class
	javac Role.java

########
# Layer 3: support classes for the Epidemic network simulator

Time.class:
	javac Time.java

Error.class:
	javac Error.java

MyScanner.class: Error.class
	javac MyScanner.java

Check.class: Error.class
	javac Check.java

MyRandom.class:
	javac MyRandom.java

Simulator.class:
	javac Simulator.java

Schedule.class: MyRandom.class MyScanner.class
	javac Schedule.java

########
# utility make commands

#html:
#	javadoc -Xdoclint:none -d ./doc *.java

html: $(allJava)
	javadoc $(allJava)
clean:
	rm -f *.class *.html package-list script.js stylesheet.css

demo:
	java Epidemic testa
	java Epidemic testb
	java Epidemic testc
	java Epidemic testd