// Place.java

import java.util.LinkedList;

/**
 * Places that people are associate with and may occupy.
 * 
 * @author Douglas W. Jones
 * @version Apr. 6, 2021 Every place is an instance of some kind of PlaceKind
 * @see PlaceKind for most of the attributes of places
 */
public class Place {
	// instance variables fixed at creation
	public final PlaceKind kind; // what kind of place is this?
	private final double transmissivity; // how dangerous is it to stay here

	// instance variables that vary with circumstances
	private int contageous = 0; // how many infectious people are here
	private final LinkedList<Person> occupants = new LinkedList<>();

	/**
	 * Construct a new place
	 * 
	 * @param k -- the kind of place
	 * @param t -- the transmissivity of the place
	 */
	public Place(PlaceKind k, Double t) {
		kind = k;
		transmissivity = t;
	}

	/**
	 * a person arrives at a place
	 * 
	 * @param time when the arrival happens
	 * @param p    the person involved
	 */
	void arrive(double time, Person p) {
		if (p.isContageous())
			contageous(time, +1);
		occupants.add(p);
	}

	/**
	 * a person departs from a place
	 * 
	 * @param time when the departure happens
	 * @param p    the person involved
	 */
	void depart(double time, Person p) {
		occupants.remove(p);
		if (p.isContageous())
			contageous(time, -1);
	}

	/**
	 * a person in this place changes contageon state
	 * 
	 * @param time at which contageon change happens
	 * @param c,   +1 means became contageous, -1 means recovered or died
	 */
	void contageous(double time, int c) {
		contageous = contageous + c;

		// when the number of contageous people in a place changes,
		for (Person p : occupants) {
			p.scheduleInfect(time, 1 / (contageous * transmissivity));
		}
	}
}