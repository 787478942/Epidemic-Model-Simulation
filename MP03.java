/* Epidemic Simulator
 * Author: Douglas Jones
 * Status: MP4 solution; it works, but see BUG notices
 * Version: 10/5/2020
 */
import java.lang.Math;
import java.lang.NumberFormatException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Random;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileNotFoundException;

// Utility classes

/**
 * Error handling
 */
class Error{
	// BUG:  Perhaps we should count warnings

	/** Report a warning to System.err
	 *  @param message the text of the warning
	 */
	public static void warn( String message ) {
		System.err.println( message + "\n" );
		// BUG:  Perhaps count warnings and error out if too many
	}

	/** Report a fatal error to System.err
	 *  @param message the text reporting the error
	 *  Note that this code exits the program with an error indication
	 */
	public static void fatal( String message ) {
		warn( message );
		System.exit( 1 );
	}
}

/**
 * Wrapper or Adapter for Scanners that integrates error handling
 * @see java.util.Scanner
 * @see Error
 */
class MyScanner {
	Scanner self; // the scanner this object wraps

	// patterns for popular scannables, compiled just once
	static Pattern delimPat = Pattern.compile( "([ \t\r\n]|(//[\\S \t]*\n))*" );
	// allow empty delimiters, and allow Java style comments
	static Pattern intPat = Pattern.compile( "[0-9]*" );
	// integers
	static Pattern realPat = Pattern.compile( "\\d*\\.?\\d*" );
	// reals
	// BUG above does not permit exponent notation as in 6.02E23

	/** Construct a MyScanner to read from a file
	 *  @param f the file to read from
	 *  @throws FileNotFoundException if the file could not be read
	 */
	public MyScanner( File f ) throws FileNotFoundException {
		self = new Scanner( f );
	}

	// methods we wish could inherit from Scanner but can't beause it's final
	// BUG -- to properly handle end of line delimiters, these need redefinition
	public boolean hasNext( String s ) { return self.hasNext( s ); }
	public boolean hasNextDouble()     { return self.hasNextFloat(); }
	public boolean hasNextFloat()      { return self.hasNextFloat(); }
	public boolean hasNextInt()        { return self.hasNextInt(); }
	public String  next( String s )    { return self.next( s ); }
	public float   nextDouble()        { return self.nextFloat(); }
	public float   nextFloat()         { return self.nextFloat(); }
	public int     nextInt()           { return self.nextInt(); }
	public String  nextLine()          { return self.nextLine(); }

	// redefined methods from class Scanner

	/** Is there a next token?
	 *  but first skip optional extended delimiters
	 *  @return true if there is a token, otherwise false
	 */
	public boolean hasNext() {
		self.skip( delimPat );          // skip the delimiter, if any
		return self.hasNext();
	}

	/** Get the next token,
	 *  but first skip optional extended delimiters
	 *  @return the token as a string
	 */
	public String  next() {
		self.skip( delimPat );          // skip the delimiter, if any
		return self.next();
	}

	// new methods we add to this class

	/** Get the next string, if one is available
	 *  @param def the default value if no string is available
	 *  @param msg the error message to print if no string is available
	 *  @return the token as a String or the default
	 */
	public String getNext( String def, String msg ) {
		if (self.hasNext()) return self.next();
		Error.warn( msg );
		return def;
	}

	/** Get the next match to pattern, if one is available
	 *  @param pat the pattern string we are trying to match
	 *  @param def the default value if no match available
	 *  @param msg the error message to print if no match available
	 *  @return the token as a String or the default
	 */
	public String getNext( String pat, String def, String msg ) {
		self.skip( delimPat );          // skip the delimiter, if any
		self.skip( "(" + pat + ")?" );  // skip the pattern if present
		String next = self.match().group();
		if (!next.isEmpty()) { // non-empty means next thing matched pat
			return next;
		} else {
			Error.warn( msg );
			return def;
		}
	}

	/** Get the next double, if one is available
	 *  @param def the default value if no float is available
	 *  @param msg the error message to print if no double is available
	 *  @return the token as a double or the default
	 */
	public double getNextDouble( double def, String msg ) {
		self.skip( delimPat ); // skip the delimiter, if any
		self.skip( realPat );  // skip the float, if any
		String next = self.match().group();
		try {
			return Double.parseDouble( next );
		} catch ( NumberFormatException e ) {
			Error.warn( msg );
			return def;
		}
	}

	/** Get the next float, if one is available
	 *  @param def the default value if no float is available
	 *  @param msg the error message to print if no float is available
	 *  @return the token as a float or the default
	 */
	public float getNextFloat( float def, String msg ) {
		self.skip( delimPat ); // skip the delimiter, if any
		self.skip( realPat );  // skip the float, if any
		String next = self.match().group();
		try {
			return Float.parseFloat( next );
		} catch ( NumberFormatException e ) {
			Error.warn( msg );
			return def;
		}
	}

	/** Get the next int, if one is available
	 *  @param def the default value if no int is available
	 *  @param msg the error message to print if no int is available
	 *  @return the token as an int or the default
	 */
	public int getNextInt( int def, String msg ) {
		self.skip( delimPat ); // skip the delimiter, if any
		self.skip( intPat );   // skip the float, if any
		String next = self.match().group();
		try {
			return Integer.parseInt( next );
		} catch ( NumberFormatException e ) {
			Error.warn( msg );
			return def;
		}
	}
}

// Model classes

/**
 * People occupy places
 * @see Place
 * @see Employee
 */
class Person {
	// private stuff needed for instances

	enum States { // BUG -- does this go here?  Is it public or private?
		uninfected, latent, bedridden, recovered, dead
		// the order of the above is significant: >= uninfected is infected
	}

	// instance variables
	private final HomePlace home;  // all people have homes
	public final String name;      // all people have names
	public States infectionState;  // all people have infection states

	// the collection of all instances
	private static final LinkedList <Person> allPeople =
			new LinkedList <Person> ();

	/** The only constructor
	 *  @param h the home of the newly constructed person
	 */
	public Person( HomePlace h ) {
		name = super.toString();
		home = h;
		infectionState = States.uninfected;
		h.addResident( this );

		allPeople.add( this ); // this is the only place items are added!
	}

	/** Infect a person
	 * called when circumstances call for a person to become infected
	 */
	public void infect() {
		// BUG -- what if this person is already infected?
		infectionState = States.latent;
		// BUG -- when simulation is added, will this launch a disease process?
	}

	/** Primarily for debugging
	 * @return textual name and home of this person
	 */
	public String toString() {
		return name + " " + home.name + " " + infectionState;
	}

	/** Shuffle the population
	 *  This allows correlations between attributes of people to be broken
	 */
	public static void shuffle() {
		Collections.shuffle( allPeople );
		// BUG -- above uses default source of randomness, is this right?
	}

	/** Allow outsiders to iterate over all people
	 * @return an iterator over people
	 */
	public static Iterator <Person> iterator() {
		return allPeople.iterator();
	}
}

/**
 * Employees are People who work
 * @see Person
 * @see WorkPlace
 */
class Employee extends Person {
	// instance variables
	private WorkPlace job;  // employees have WorkPlaces
	// can't be final because set post constructor

	/** The only constructor
	 *  @param h the HomePlace of the newly constructed Employee
	 *  Note that employees are created without well-defined workplaces
	 */
	public Employee( HomePlace h ) {
		super( h ); // construct the base person
		job = null;
	}

	/** Set workplace of employee
	 *  @param w the workPlace of the newly constructed Employee
	 *  No employee's workplace may be set more than once
	 */
	public void setWorkplace( WorkPlace w ) {
		assert job == null;
		job = w;
		w.addEmployee( this );
	}

	/** Primarily for debugging
	 * @return textual name home and employer of this person
	 */
	public String toString() {
		return super.toString() + " " + job.name;
	}
}

/**
 * Places are occupied by people
 * @see HomePlace
 * @see WorkPlace
 */
abstract class Place {
	// instance variables
	public final String name;

	// contructor (effectively protected
	Place() {
		name = super.toString();
		allPlaces.add( this );
	}

	// the collection of all instances
	private static final LinkedList <Place> allPlaces =
			new LinkedList <Place> ();

	/** Allow outsiders to iterate over all places
	 * @return an iterator over places
	 */
	public static Iterator <Place> iterator() {
		return allPlaces.iterator();
	}
}

/**
 * HomePlaces are occupied by any type of person
 * @see Place
 * @see Person
 */
class HomePlace extends Place {
	private final LinkedList <Person> residents = new LinkedList <Person> ();

	/** The only constructor for Place
	 *  Places are constructed with no occupants
	 */
	public HomePlace() {
		super(); // initialize the underlying place
	}

	/** Add a resident to a place
	 *  Should only be called from the person constructor
	 *  @param r a Person, the new resident
	 */
	public void addResident( Person r ) {
		residents.add( r );
		// no need to check to see if the person already lives there?
	}

	/** Primarily for debugging
	 * @return textual name and employees of the workplace
	 */
	public String toString() {
		String res = name;
		for (Person p: residents) { res = res + " " + p.name; }
		return res;
	}
}

/**
 * WorkPlaces are occupied by employees
 * @see Place
 * @see Employee
 */
class WorkPlace extends Place {
	private final LinkedList <Employee> employees = new LinkedList <Employee>();

	/** The only constructor for WorkPlace
	 *  WorkPlaces are constructed with no residents
	 */
	public WorkPlace() {
		super(); // initialize the underlying place
	}

	/** Add an employee to a WorkPlace
	 *  Should only be called from the person constructor
	 *  @param r an Employee, the new worker
	 */
	public void addEmployee( Employee r ) {
		employees.add( r );
		// no need to check to see if the person already works there?
	}

	/** Primarily for debugging
	 * @return textual name and employees of the workplace
	 */
	public String toString() {
		String res = name;
		for (Employee p: employees) { res = res + " " + p.name; }
		return res;
	}
}

/**
 * Main class builds model and will someday simulate it
 * @see Person
 * @see Place
 */
public class Epidemic {

	// the following are set by readCommunity and used by buildCommunity
	// default values are used to check for failure to initialize

	static int pop = -1;         /* the target population */
	static double houseMed = -1; /* median household size */
	static double houseSc = -1;  /* household size scatter */
	static double workMed = -1;  /* median workplace size */
	static double workSc = -1;   /* workplace size scatter */
	static int infected = -1;    /* the target number of infected people */
	static double employed = -1; /* the likelihood that someone is employed */

	/** Read and check the simulation parameters
	 *  @param sc the scanner to read the community description from
	 *  Called only from the main method.
	 */
	private static void readCommunity( MyScanner sc ) {

		while (sc.hasNext()) {
			// until the input file is finished
			String command = sc.next();
			if ("pop".equals( command )) {
				if (pop > 0) Error.warn( "population already set" );
				pop = sc.getNextInt( 0, "pop with no argument" );
				sc.getNext( ";", "", "pop " +pop+ ": missing semicolon" );

			} else if ("house".equals( command )) {
				if (houseMed > 0) Error.warn( "household size already set" );
				if (houseSc >= 0) Error.warn( "household scatter already set" );
				houseMed = sc.getNextDouble( 1, "house with no argument" );
				sc.getNext( ",", "", "house " +houseMed+ ": missing comma" );
				houseSc = sc.getNextDouble( 1,
						"house " +houseMed+ ", missing argument "
				);
				sc.getNext( ";", "",
						"house " +houseMed+ ", " +houseSc+ ": missing semicolon"
				);

			} else if ("workplace".equals( command )) {
				if (workMed > 0) Error.warn( "workplace size already set" );
				if (workSc >= 0) Error.warn( "workplace scatter already set" );
				workMed = sc.getNextDouble( 1, "workplace with no argument" );
				sc.getNext( ",", "",
						"workplace " +houseMed+ ": missing comma"
				);
				workSc = sc.getNextDouble( 1,
						"workplace " +houseMed+ ", missing argument "
				);
				sc.getNext( ";", "",
						"workplace " +houseMed+ ", " +houseSc+ ": missing semicolon"
				);

			} else if ("infected".equals( command )) {
				if (infected > 0) Error.warn( "infected already set" );
				infected = sc.getNextInt( 1, "infected with no argument" );
				sc.getNext( ";", "",
						"infected " +infected+ ": missing semicolon"
				);

			} else if ("employed".equals( command )) {
				if (employed >= 0) Error.warn( "employed rate already set" );
				employed = sc.getNextDouble( 1, "employed with no argument" );
				sc.getNext( ";", "",
						"employed" +employed+ ": missing semicolon"
				);

			} else {
				Error.warn( "unknown command: " +command+ "\n" );
			}
		}

		// BUG -- if there were errors, it might be best to quit now

		// check for complete initialization
		if (pop < 0) Error.warn( "population not initialized" );
		if (houseMed <= 0) Error.warn( "median household size not set" );
		if (houseSc < 0) Error.warn( "household scatter not set" );
		if (workMed <= 0) Error.warn( "median workplace size not set" );
		if (workSc < 0) Error.warn( "workplace scatter not set" );
		if (infected < 0) Error.warn( "infected number not given" );
		if (infected > pop) Error.warn( "infected number exceeds population" );
		if (employed < 0) Error.warn( "employment rate not given" );
		if (employed > 1) Error.warn( "employment rate impossibly high" );
	}

	/** Build a community that the simulation parameters describe
	 *  Called only from the main method.
	 */
	private static void buildCommunity() {
		// must always have a home available as we create people
		int currentHomeCapacity = 0;
		int currentWorkCapacity = 0;
		HomePlace currentHome = null;
		WorkPlace currentWork = null;

		// need a source of random numbers // BUG -- does this go here?
		Random rand = new Random();

		// parameters needed for random distributions
		double houseSig = Math.log( (houseSc + houseMed) / houseMed );
		double workSig = Math.log( (workSc + workMed) / workMed );

		// create the population
		for (int i = 0; i < pop; i++) {
			Person p = null;
			if (currentHomeCapacity < 1) { // must create a new home
				currentHome = new HomePlace();
				currentHomeCapacity = (int)Math.ceil(
						Math.exp( houseSig * rand.nextGaussian() ) * houseMed
				);
			}
			currentHomeCapacity = currentHomeCapacity - 1;

			// create the right kind of person
			if (rand.nextDouble() <= employed) { // this is as an employee
				p = new Employee( currentHome );
			} else { // this is an unemployed generic person
				p = new Person( currentHome );
			}

			// decide who to infect
			//   note: pop - i = number of people not yet considered to infect
			//   and   infected = number we need to infect, always <= (pop - i)
			if (rand.nextInt( pop - i ) < infected) {
				p.infect();
				infected = infected - 1;
			}
		}

		Person.shuffle(); // shuffle the population to break correlations

		// go through the population again
		for (Iterator<Person> i = Person.iterator(); i.hasNext(); ){
			Person p = i.next(); // for each person
			if (p instanceof Employee) {
				Employee e = (Employee)p;
				if (currentWorkCapacity < 1) { // must create new workplace
					currentWork = new WorkPlace();
					currentWorkCapacity = (int)Math.ceil(
							Math.exp( workSig * rand.nextGaussian() ) * workMed
					);
				}
				currentWorkCapacity = currentWorkCapacity - 1;
				e.setWorkplace( currentWork );
			}
		}
	}

	/** Output the community
	 * Called only from the main method.
	 * This code exists only for debugging.
	 */
	private static void writeCommunity() {

		System.out.println( "People" ); // Note:  Not required in assignment
		for (Iterator<Person> i = Person.iterator(); i.hasNext(); ){
			System.out.println( i.next().toString() );
		}

		System.out.println( "Places" ); // Note:  Not required in assignment
		for (Iterator<Place> i = Place.iterator(); i.hasNext(); ){
			System.out.println( i.next().toString() );
		}
	}

	/** The main method
	 *  This handles the command line arguments.
	 *  If the args are OK, it calls other methods to build and test a model.
	 */
	public static void main( String[] args ) {
		if (args.length < 1) {
			Error.fatal( "Missing file name argument\n" );
		} else try {
			readCommunity( new MyScanner( new File( args[0] ) ) );
			// BUG -- if there were errors, perhaps should not do folloing?
			buildCommunity();  // build what was read above
			writeCommunity();  // BUG -- this is just for debugging
			// BUG -- need to actually add simulation code here
		} catch ( FileNotFoundException e) {
			Error.fatal( "Can't open file: " + args[0] + "\n" );
		}
	}
}