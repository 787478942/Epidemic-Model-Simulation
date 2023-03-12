// PlaceKind.java

import java.util.Collections;
import java.util.LinkedList;

/**
 * Categories of places
 * 
 * @author Douglas W. Jones
 * @version Apr. 6, 2021
 * @see Place
 * @see MyRandom
 * @see MyScanner
 */
public class PlaceKind {

	// linkage from person to associated place involves a schedule
	private class PersonSchedule {
		public Person person;
		public Schedule schedule;

		public PersonSchedule(Person p, Schedule s) {
			person = p;
			schedule = s;
		}
	}

	// instance variables from the input
	final String name; // the name of this category of place
	private double median; // median population for this category
	private double scatter;// scatter of size distribution, reduces to sigma
	private double transmissivity; // how likely is disease transmission here

	// instance variables developed during model elaboration
	private double sigma; // sigma of the log normal population distribution
	private Place unfilledPlace = null; // a place of this kind being filled
	private int unfilledCapacity = 0; // capacity of unfilledPlace

	// a list of all the people associated with this kind of place
	private final LinkedList<PersonSchedule> people = new LinkedList<>();

	// static variables used for categories of places
	private static LinkedList<PlaceKind> allPlaceKinds = new LinkedList<>();
	private static final MyRandom rand = MyRandom.stream();

	/**
	 * Construct a new place category by scanning an input stream
	 * 
	 * @param in -- the input stream The stream must contain the category name, and
	 *           the parameters for a log-normal distribution for the sizes. All
	 *           specifications end with a semicolon.
	 */
	public PlaceKind(MyScanner in) {

		name = in.getNextName("???", () -> "place with no name");
		median = in.getNextFloat(9.9999F, () -> "place " + name + ": not followed by median");
		scatter = in.getNextFloat(9.9999F, () -> "place " + name + " " + median + ": not followed by scatter");
		transmissivity = (1 / Time.hour) * in.getNextFloat(9.9999F,
				() -> "place " + name + " " + median + " " + scatter + ": not followed by transmissivity"); // BUG:
																											// conversion
																											// factors
																											// this is
																											// given in
																											// per
																											// hour!!!
		in.getNextLiteral(MyScanner.semicolon, () -> this.describe() + ": missing semicolon");

		// complain if the name is not unique
		if (findPlaceKind(name) != null) {
			Error.warn(this.describe() + ": duplicate name");
		}
		// force the median to be positive
		median = Check.positive(median, 1.0F, () -> this.describe() + ": non-positive median?");
		// force the scatter to be positive or zero
		scatter = Check.nonNeg(scatter, 0.0F, () -> this.describe() + ": negative scatter?");
		// force the transmissivity to be positive or zero
		transmissivity = Check.nonNeg(transmissivity, 0.0F, () -> this.describe() + ": negative scatter?");

		sigma = Math.log((scatter + median) / median);
		allPlaceKinds.add(this); // include this in the list of all
	}

	/**
	 * Produce a reasonable textual description of this place
	 * 
	 * @return the description This shortens many error messages
	 */
	private String describe() {
		return "place " + name + " " + median + " " + scatter + " " + transmissivity;
	}

	/**
	 * Find or make a place of a particular kind
	 * 
	 * @return the place This should be called when a person is to be linked to a
	 *         place of some particular kind, potentially occupying a space in that
	 *         place.
	 */
	private Place findPlace() {
		if (unfilledCapacity <= 0) { // need to make a new place
			// make new place using a log-normal distribution for the size
			unfilledCapacity = (int) Math.round(rand.nextLogNormal(median, sigma));
			unfilledPlace = new Place(this, transmissivity);
		}
		unfilledCapacity = unfilledCapacity - 1;
		return unfilledPlace;
	}

	/**
	 * Add a person to the population of this kind of place
	 * 
	 * @param p the new person
	 * @param s the associated schedule
	 */
	public void populate(Person p, Schedule s) {
		people.add(new PersonSchedule(p, s));
	}

	/**
	 * Distribute the people from all PlaceKinds to their individual places Prior to
	 * this, each PlaceKind knows all the people that will be associated with places
	 * of that kind, a list constructed by populate(). This calls findPlace to
	 * create or find places.
	 */
	public static void distributePeople() {

		// for each kind of place
		for (PlaceKind pk : allPlaceKinds) {
			// shuffle its people to break correlations from people to places
			Collections.shuffle(pk.people, MyRandom.stream);

			// for each person, associate that person with a specific place
			for (PersonSchedule ps : pk.people) {
				ps.person.emplace(pk.findPlace(), ps.schedule);
			}
		}
	}

	/**
	 * Find a category of place, by name
	 * 
	 * @param n -- the name of the category
	 * @return the PlaceKind with that name, or null if none has been defined
	 */
	public static PlaceKind findPlaceKind(String n) {
		for (PlaceKind pk : allPlaceKinds) {
			if (pk.name.equals(n))
				return pk;
		}
		return null; // category not found
	}
}