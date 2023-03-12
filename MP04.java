// Epidemic.java
/* Program that will eventually develop into an epidemic simulator
 * author Douglas W. Jones
 * version Feb. 28, 2021
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;

/** Error reporting framework
 *  All error messages go to System.err (aka stderr, the standard error stream).
 *  Currently, this only supports fatal error reporting.
 *  Later it would be nice to have a way to report non-fatal errors.
 */
class Error {
    private static int warningCount = 0;

    /** Report a fatal error
     *  @param msg -- error message to be output
     *  This never returns, the program terminates reporting failure.
     */
    public static void fatal( String msg ) {
	System.err.println( "Epidemic: " + msg );
	System.exit( 1 );  // abnormal termination
    }

    /** Non-fatal warning
     *  @param msg -- the warning message
     *  keeps a running count of warnings
     */
    public static void warn( String msg ) {
	System.err.println( "Warning: " + msg );
	warningCount = warningCount + 1;
    }

    /** Error exit if any warnings
     */
    public static void exitIfWarnings( String msg ) {
	if (warningCount > 0) fatal( msg );
    }
}

/** Support for scanning input files with error reporting
 *  @see Error
 *  @see java.util.Scanner
 *  Ideally, this would be extend class Scanner, but class Scanner is final
 *  Therefore, this is a wrapper class around class Scanner
 */
class MyScanner {
    private Scanner sc; // the scanner we are wrapping

    public MyScanner( File f ) throws FileNotFoundException {
	sc = new Scanner( f );
    }

    // methods that we wish we could inhereit from Scanner
    public boolean hasNext() { return sc.hasNext(); }
    public boolean hasNext( String s ) { return sc.hasNext( s ); }
    public String next() { return sc.next(); }

    // new methods added to class Scanner

    /** get the next item from the scanner or complain if missing
     *  @param defalt  -- return value if there is no next item
     *  @param errorMesage -- the message to complain with
     *  @return the next item or the defalt
     */
    public String getNext( String defalt, String errorMessage ) {
	if ( sc.hasNext() ) {
	    return sc.next();
	} else {
	    Error.warn( errorMessage );
	    return defalt;
	}
    }

    /** get the next integer from the scanner or complain if missing
     *  @param defalt  -- return value if there is no next integer
     *  @param errorMesage -- the message to complain with
     *  @return the next integer or the defalt
     */
    public int getNextInt( int defalt, String errorMessage ) {
	if ( sc.hasNextInt() ) {
	    return sc.nextInt();
	} else {
	    Error.warn( errorMessage );
	    return defalt;
	}
    }

    /** get the next float from the scanner or complain if missing
     *  @param defalt  -- return value if there is no next float
     *  @param errorMesage -- the message to complain with
     *  @return the next float or the defalt
     */
    public float getNextFloat( float defalt, String errorMessage ) {
	if ( sc.hasNextFloat() ) {
	    return sc.nextFloat();
	} else {
	    Error.warn( errorMessage );
	    return defalt;
	}
    }

    /** get the next literal from the scanner or complain if missing
     *  @param literal -- the literal to get
     *  @param errorMesage -- the message to complain with
     */
    public void getNextLiteral( String literal, String errorMessage ) {
	if ( sc.hasNext( literal ) ) {
	    sc.next( literal );
	} else {
	    Error.warn( errorMessage );
	}
    }
}

/** Class for semantic error checkers
 *  @see Error
 *  This is a place to put error checking code that doesn't fit elsewhere.
 *  The error check methods here actually take up more space than the
 *  code they helped clarify, so the net gain in readability for this code
 *  is rather limited.  Perhaps as the program grows, they'll help more.
 */
class Check {

    /** Force a floating value to be positive
     *  @param value -- the value to check
     *  @param defalt -- the value to use if the check fails
     *  @param msg -- the error message to output if check fails
     *  @return either value if success or defalt if failure
     */
    public static float positive( float value, float defalt, String msg ) {
	if (value > 0.0) {
	    return value;
	} else {
	    Error.warn( msg );
	    return defalt;
	}
    }

    /** Force a floating value to be non negative
     *  @param value -- the value to check
     *  @param defalt -- the value to use if the check fails
     *  @param msg -- the error message to output if check fails
     *  @return either value if success or defalt if failure
     */
    public static float nonNegative( float value, float defalt, String msg ) {
	if (value >= 0.0) {
	    return value;
	} else {
	    Error.warn( msg );
	    return defalt;
	}
    }
}

/** Wrapper extending class Random, turning it into a singleton class
 *  @see Random
 *  Ideally, no user should ever create an instance of Random, all use this!
 *  Users can call MyRandom.stream.anyMethodOfRandom() (or of MyRandom)
 *              or MyRandom.stream().anyMethodOfRandom()
 *  Users can allocate MyRandom myStream = MyRandom.stream;
 *                  or MyRandom myStream = MyRandom.stream();
 *  No matter how they do it, they get the same stream
 */
class MyRandom extends Random {
    /** the only random number stream
     */
    public static final MyRandom stream = new MyRandom(); // the only stream;

    // nobody can construct a MyRandom except the above line of code
    private MyRandom() {
	super();
    }

    /* alternative access to the only random number stream
     * @return the only stream
     */
    public static MyRandom stream() {
	return stream;
    }

    // add distributions that weren't built in

    /** exponential distribution
     *  @param mean -- the mean value of the distribution
     *  @return a positive exponentially distributed random value
     */
    public double nextExponential( double mean ) {
        return mean * -Math.log( this.nextDouble() );
    }
}

/** Places that people are associate with and may occupy.
 *  Every place is an instance of some kind of PlaceKind
 *  @see PlaceKind for most of the attributes of places
 */
class Place {
    // instance variables
    final PlaceKind kind; // what kind of place is this?

    /** Construct a new place
     *  @param k -- the kind of place
     *  BUG:  Attributes such as disease transmissivity will be needed
     */
    public Place( PlaceKind k ) {
	kind = k;
    }
}

/** Categories of places
 *  @see Place
 */
class PlaceKind {
    // instance variables
    final String name;    // the name of this category of place
    private float median; // median population for this category
    private float scatter;// scatter of size distribution for this
    private float sigma;  // sigma of the log normal distribution
    private Place unfilledPlace = null; // a place of this kind being filled
    private int unfilledCapacity = 0;   // capacity of unfilledPlace

    // a list of all the people associated with this kind of place
    private final LinkedList<Person> people = new LinkedList<>();

    // static variables used for categories of places
    private static LinkedList<PlaceKind> allPlaceKinds = new LinkedList<>();
    private MyRandom rand = MyRandom.stream();

    /** Construct a new place category by scanning an input stream
     *  @param in -- the input stream
     *  The stream must contain the category name, and the parameters
     *  for a log-normal distribution for the sizes.
     *  All specifications end with a semicolon.
     */
    public PlaceKind( MyScanner in ) {

	name = in.getNext( "???", "place with no name" );
	median = in.getNextFloat(
	    9.9999F,
	    "place " + name + ": not followed by median"
	);
	scatter = in.getNextFloat(
	    9.9999F,
	    "place " + name + " " + median + ": not followed by scatter"
	);
	in.getNextLiteral( ";", this.describe() + ": missing semicolon" );

	// complain if the name is not unique
	if (findPlaceKind( name ) != null) {
	    Error.warn( this.describe() + ": missing semicolon");
	}
	// force the median to be positive
	median = Check.positive( median, 1.0F,
	    this.describe() + ": non-positive median?"
	);
	// force the scatter to be positive
	scatter = Check.nonNegative( scatter, 0.0F,
	    this.describe() + ": negative scatter?"
	);
	sigma = (float)Math.log( (scatter + median) / median );

	allPlaceKinds.add( this ); // include this in the list of all
    }

    /** Produce a full textual description of this place
     *  @return the description
     *  This shortens many error messages
     */
    private String describe() {
	return "place " + name + " " + median + " " + scatter;
    }

    /** Find or make a place of a particular kind
     *  @return the place
     *  This should be called when a person is to be linked to a place of some
     *  particular kind, potentially occupying a space in that place.
     */
    private Place findPlace() {
	if (unfilledCapacity <= 0 ) { // need to make a new place
	    // must make a new place using a log-normal distribution
	    double lognormal = Math.exp( sigma * rand.nextGaussian() ) * median;
	    unfilledCapacity = (int)Math.round( lognormal );
	    unfilledPlace = new Place( this );
	}
	unfilledCapacity = unfilledCapacity - 1;
	return unfilledPlace;
    }

    /** Add a person to the population of this kind of place
     *  @param p the new person
     */
    public void populate( Person p ) {
	people.add( p );
    }

    /** Distribute the people from all PlaceKinds to their individual places
     *  Prior to this, each PlaceKind knows all the people that will be
     *  associated with places of that kind, a list constructed by populate().
     *  This calls findPlace to create or find places.
     */
    public static void distributePeople() {

	// for each kind of place
	for (PlaceKind pk: allPlaceKinds) {
	    // shuffle its people to break correlations from people to places
	    Collections.shuffle( pk.people, MyRandom.stream );

	    // for each person, associate that person with a specific place
	    for (Person p: pk.people) {
		p.emplace( pk.findPlace() );
	    }
	}
    }

    /** Find a category of place, by name
     *  @param n -- the name of the category
     *  @return the PlaceKind with that name, or null if none has been defined
     */
    public static PlaceKind findPlaceKind( String n ) {
	for (PlaceKind pk: allPlaceKinds) {
	    if (pk.name.equals( n )) return pk;
	}
	return null; // category not found
    }
}

/** People in the simulated community each have a role
 *  @see Person
 *  @see PlaceKind
 *  Roles create links from people to the categories of places they visit
 */
class Role {

    // instance variables
    public final String name; // name of this role
    private final LinkedList<PlaceKind> placeKinds = new LinkedList<>();

    private float fraction;   // fraction of the population in this role
    private int number;       // number of people in this role

    // static variables used for summary of all roles
    private static float sum = 0.0F; // sum of all the fractions
    private static LinkedList<Role> allRoles = new LinkedList<Role>();

    /** Construct a new role by scanning an input stream
     *  @param in -- the input stream
     *  The stream must contain the role name, and the number or fraction
     *  of the population in that role.
     *  All role specifications end with a semicolon.
     */
    public Role( MyScanner in ) {

	name = in.getNext( "???", "role with no name" );
	fraction = in.getNextFloat(
	    9.9999F,
	    "role " + name + ": not followed by population"
	);

	// get the list of places associated with this role
	while (in.hasNext() && !in.hasNext( ";" )) {
	    String placeName = in.next();
	    PlaceKind p = PlaceKind.findPlaceKind( placeName );

	    // see if this place is can be legally associated with this role
	    if (p == null) {
		Error.warn(
		    this.describe() + " " + placeName +
		    ": place name undefined?"
		);
	    } else {
		// see if this role is already associated with this PlaceKind
		boolean duplicated = false;
		for (PlaceKind pp: placeKinds) {
		    if (p == pp) duplicated = true;
		}
		if (duplicated) {
		    Error.warn(
			this.describe() + " " + placeName +
			": place name reused?"
		    );
		} else {
		    placeKinds.add( p );
		}
	    }
	}

	in.getNextLiteral( ";", this.describe() + ": missing ;" );

	// complain if the name is not unique
	if (findRole( name ) != null) {
	    Error.warn( this.describe() + ": role name reused?" );
	}
	// force the fraction or population to be positive
	fraction = Check.positive( fraction, 0.0F,
	    this.describe() + ": negative population?"
	);
	sum = sum + fraction;
	// complain if no places for this role
	if (placeKinds.isEmpty()) {
	    Error.warn( this.describe() + ": has no places?" );
	}

	allRoles.add( this ); // include this role in the list of all roles
    }

    /** Produce a reasonably full textual description of this role
     *  @return the description
     *  This shortens many error messages
     */
    private String describe() {
	return "role " + name + " " + fraction;
    }

    /** Find a role, by name
     *  @param n -- the name of the role
     *  @return the role with that name, or null if none has been defined
     */
    private static Role findRole( String n ) {
	for (Role r: allRoles) {
	    if (r.name.equals( n )) return r;
	}
	return null; // role not found
    }

    /** Create the total population, divided up by roles in
     *  @param population -- the total population to be created
     *  The math here divides the population in the ratio of the numbers
     *  given for each role.
     *  It is critical that this not be done until all roles are known.
     */
    public static void populateRoles( int population ) {
	if (allRoles.isEmpty()) Error.fatal( "no roles specified" );
	for (Role r: allRoles) {
	    // how many people are in this role
	    r.number = Math.round( (r.fraction / r.sum) * population );

	    // make that many people
	    for (int i = 1; i <= r.number; i++) {
		Person p = new Person( r );

		// each person is associated all their role's place kinds
		// note that this does not create places yet
		for (PlaceKind pk: r.placeKinds) {
		    pk.populate( p );
		}
	    }
	}

	// finish putting people in their places
	// this actually creates the places and puts people in them
	PlaceKind.distributePeople();
    }
}

/** People are the central actors in the simulation
 *  @see Role for the roles people play
 *  @see Place for the places people visit
 */
class Person {
    // instance variables
    private final Role role;      // role of this person
    private final LinkedList<Place> places = new LinkedList<Place>();

    // static variables used for all people
    private static LinkedList<Person> allPeople = new LinkedList<Person>();

    /** Construct a new person to perform some role
     *  @param r -- the role
     *  This constructor deliberately defers putting people in any places
     */
    public Person( Role r ) {
	role = r;

	allPeople.add( this ); // include this person in the list of all
    };

    /** Associate this person to a particular place
     *  @param p -- the place
     */
    public void emplace( Place p ) {
	places.add( p );
    }

    /** Print out the entire population
     *  This is needed only in the early stages of debugging
     *  and obviously useless for large populations
     */
    public static void printAll() {
	for (Person p: allPeople) {
	    System.out.print( p.toString() + " " + p.role.name );
	    for (Place pp: p.places ) {
		System.out.print( " " + pp.kind.name + " " + pp.toString() );
	    }
	    System.out.println();
	}
    }
}

/** The main class
 *  This class should never be instantiated.
 *  All methods here are static and all but the main method are private.
 *  @see Role for the framework that creates people
 *  @see PlaceKind for the framework from which places are constructed
 *  @see Person for the ultimate result of this creation
 */
public class Epidemic {

    /** Read the details of the model from an input stream
     *  @param in -- the stream
     *  Identifies the keywords population, role, etc and farms out the
     *  work for most of these to the classes that construct model parts.
     *  The exception (for now) is the total population.
     */
    private static void buildModel( MyScanner in ) {
	int pop = 0; // the population of the model, 0 = uninitialized

	while ( in.hasNext() ) { // scan the input file

	    // each item begins with a keyword
	    String keyword = in.next();
	    if ("population".equals( keyword )) {
		if (pop != 0) {
		    Error.warn( "population specified more than once" );
		}
		pop = in.getNextInt( 1, "population: missing integer" );
		if (pop <= 0) {
		    Error.warn( "population " + pop + ": not positive" );
		    pop = 1;
		}
		in.getNextLiteral( ";", "population " + pop + ": missing ;" );
	    } else if ("role".equals( keyword )) {
		new Role( in );
	    } else if ("place".equals( keyword )) {
		new PlaceKind( in );
	    } else { // none of the above
		Error.warn( "not a keyword: " + keyword );
	    }
	}

	if (pop == 0) Error.warn( "population not specified" );
	Error.exitIfWarnings( "Aborted due to errors in input" );

	// Role is responsible for figuring out how many people per role
	Role.populateRoles( pop );
    }

    /** The main method
     *  @param args -- the command line arguments
     *  Most of this code is entirely about command line argument processing.
     *  It calls buildModel and will eventuall also start the simulation.
     */
    public static void main( String[] args ) {
	if (args.length < 1) Error.fatal( "missing file name" );
	if (args.length > 1) Error.warn( "too many arguments: " + args[1] );
	try {
	    buildModel( new MyScanner( new File( args[0] ) ) );
	    // BUG:  Simulate based on model just built?
	    Person.printAll(); // BUG:  In the long run, this is just for debug
	} catch ( FileNotFoundException e ) {
	    Error.fatal( "could not open file: " + args[0] );
	}
    }
}
