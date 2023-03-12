// Epidemic.java
/* Program that will eventually develop into an epidemic simulator
 * author Douglas W. Jones
 * version Mar. 15, 2021
 *
 * Note:  This solution to MP5 also eliminates unnecessary concatenations
 * in error message text by packaging them as lambda expressions so that
 * the concatenations are only done if the error message actually needs
 * to be printed.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;

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

    // patterns that matter here

    // delimiters are spaces, tabs, newlines and carriage returns
    private static final Pattern delimPat = Pattern.compile( "[ \t\n\r]*" );

    // note that all of the following patterns allow an empty string to match
    // this is used in error detection below

    // if it's not a name, it begins with a non-letter
    private static final Pattern NotNamePat
	= Pattern.compile( "([^A-Za-z]*)|" );

    // names consist of a letter followed optionally by letters or digits
    private static final Pattern namePat
	= Pattern.compile( "([A-Za-z][0-9A-Za-z]*)|" );

    // if it's not an int, it begins with a non-digit, non-negative-sign
    private static final Pattern NotIntPat
	= Pattern.compile( "([^-0-9]*)|" );

    // ints consist of an optional sign followed by at least one digit
    private static final Pattern intPat = Pattern.compile(
	"((-[0-9]|)[0-9]*)"
    );

    // floats consist of an optional sign followed by
    // at least one digit, with an optional point before between or after them
    private static final Pattern floatPat = Pattern.compile(
     "-?(([0-9]+\\.[0-9]*)|(\\.[0-9]+)|([0-9]*))"
    );

    /** tool to defer computation of messages output by methods of MyScanner
     *  To pass a specific message, create a subclass of Message to do it
     *  In general, this will be used to create lambda expressions, so
     *  users will not need to even know the class name!
     */
    public interface Message {
	String myString();
    }

    // new methods added to class Scanner

    /** get the next nae from the scanner or complain if missing
     *  See namePat for the details of what makes a float.
     *  @param defalt  -- return value if there is no next item
     *  @param errorMesage -- the message to complain with (lambda expression)
     *  @return the next item or the defalt
     */
    public String getNextName( String defalt, Message errorMessage ) {
	// first skip the delimiter, accumulate anything that's not a name
	String notName = sc.skip( delimPat ).skip( NotNamePat ).match().group();

	// second accumulate the name
	String name = sc.skip( namePat ).match().group();

	if (!notName.isEmpty()) { // there's something else a name belonged
	    Error.warn(
		errorMessage.myString() + ": name expected, skipping " + notName
	    );
	}

	if (name.isEmpty()) { // missing name
	    Error.warn( errorMessage.myString() );
	    return defalt;
	} else { // there was a name
	    return name;
	}
    }

    /** get the next integer from the scanner or complain if missing
     *  See intPat for the details of what makes a float.
     *  @param defalt  -- return value if there is no next integer
     *  @param errorMesage -- the message to complain with (lambda expression)
     *  @return the next integer or the defalt
     */
    public int getNextInt( int defalt, Message errorMessage ) {
	// first skip the delimiter, accumulate anything that's not an int
	String notInt = sc.skip( delimPat ).skip( NotIntPat ).match().group();

	// second accumulate the int, if any
	String text = sc.skip( delimPat ).skip( intPat ).match().group();

	if (!notInt.isEmpty()) { // there's something else where an int belonged
	    Error.warn(
		errorMessage.myString() + ": int expected, skipping " + notInt
	    );
	}

	if (text.isEmpty()) { // missing name
	    Error.warn( errorMessage.myString() );
	    return defalt;
	} else { // the name was present and it matches intPat
	    return Integer.parseInt( text );
	}
    }

    /** get the next float from the scanner or complain if missing
     *  See floatPat for the details of what makes a float.
     *  @param defalt  -- return value if there is no next integer
     *  @param defalt  -- return value if there is no next float
     *  @param errorMesage -- the message to complain with (lambda expression)
     *  @return the next float or the defalt
     */
    public float getNextFloat( float defalt, Message errorMessage ) {
	// skip the delimiter, if any, then the float, if any; get the latter
	String text = sc.skip( delimPat ).skip( floatPat ).match().group();

	if (text.isEmpty()) { // missing name
	    Error.warn( errorMessage.myString() );
	    return defalt;
	} else { // the name was present and it matches intPat
	    return Float.parseFloat( text );
	}
    }

    // patterns for use with the NextLiteral routines
    public static final Pattern beginParen = Pattern.compile( "\\(|" );
    public static final Pattern endParen = Pattern.compile( "\\)|" );
    public static final Pattern dash = Pattern.compile( "-|" );
    public static final Pattern semicolon = Pattern.compile( ";|" );

    /** try to get the next literal from the scanner
     *  @param literal -- the literal to get
     *  @returns true if the literal was present and skipped, false otherwise
     *  The literal parameter must be a pattern that can match the empty string
     *  if the desired literal is not present.
     */
    public boolean tryNextLiteral( Pattern literal ) {
	sc.skip( delimPat ); // allow delimiter before literal!
	String s = sc.skip( literal ).match().group();
	return !s.isEmpty();
    }

    /** get the next literal from the scanner or complain if missing
     *  @param literal -- the literal to get
     *  @param errorMesage -- the message to complain with (lambda expression)
     *  @see tryNextLiteral for the mechanism used.
     */
    public void getNextLiteral( Pattern literal, Message errorMessage ) {
	if ( !tryNextLiteral( literal ) ) {
	    Error.warn( errorMessage.myString() );
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

    /** tool to defer computation of messages output by methods of Check
     *  To pass a specific message, create a subclass of Message to do it
     *  In general, this will be used to create lambda expressions, so
     *  users will not need to even know the class name!
     */
    public interface Message {
	String myString();
    }

    /** Force a floating value to be positive
     *  @param value -- the value to check
     *  @param defalt -- the value to use if the check fails
     *  @param msg -- the error message to output if check fails
     *  @return either value if success or defalt if failure
     */
    public static float positive( float value, float defalt, Message msg ) {
	if (value > 0.0) {
	    return value;
	} else {
	    Error.warn( msg.myString() );
	    return defalt;
	}
    }

    /** Force a floating value to be non negative
     *  @param value -- the value to check
     *  @param defalt -- the value to use if the check fails
     *  @param msg -- the error message to output if check fails
     *  @return either value if success or defalt if failure
     */
    public static float nonNegative( float value, float defalt, Message msg ) {
	if (value >= 0.0) {
	    return value;
	} else {
	    Error.warn( msg.myString() );
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

/** All about simulated time
 */
class Time {
    /** one second of simulated time */
    public static float second = 1.0F;

    /** one minute of simulated time */
    public static float minute = 60.0F * second;

    /** one hour of simulated time */
    public static float hour = 60.0F * minute;

    /** one day of simulated time */
    public static float day = 24.0F * hour;
}

/** Places that people are associate with and may occupy.
 *  Every place is an instance of some kind of PlaceKind
 *  @see PlaceKind for most of the attributes of places
 */
class Place {
    // instance variables
    public final PlaceKind kind; // what kind of place is this?

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

    // linkage from person to associated place involves a schedule
    private class PersonSchedule {
	public Person person;
	public Schedule schedule;
	public PersonSchedule( Person p, Schedule s ) {
	    person = p;
	    schedule = s;
	}
    }

    // instance variables
    final String name;    // the name of this category of place
    private float median; // median population for this category
    private float scatter;// scatter of size distribution for this
    private float sigma;  // sigma of the log normal distribution
    private Place unfilledPlace = null; // a place of this kind being filled
    private int unfilledCapacity = 0;   // capacity of unfilledPlace

    // a list of all the people associated with this kind of place
    private final LinkedList<PersonSchedule> people = new LinkedList<>();

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

	name = in.getNextName( "???", ()->"place with no name" );
	median = in.getNextFloat(
	    9.9999F,
	    ()->"place " + name + ": not followed by median"
	);
	scatter = in.getNextFloat(
	    9.9999F,
	    ()->"place " + name + " " + median + ": not followed by scatter"
	);
	in.getNextLiteral(
	    MyScanner.semicolon,
	    ()->this.describe() + ": missing semicolon"
	);

	// complain if the name is not unique
	if (findPlaceKind( name ) != null) {
	    Error.warn( this.describe() + ": duplicate name" );
	}
	// force the median to be positive
	median = Check.positive( median, 1.0F,
	    ()-> this.describe() + ": non-positive median?"
	);
	// force the scatter to be positive
	scatter = Check.nonNegative( scatter, 0.0F,
	    ()-> this.describe() + ": negative scatter?"
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
     *  @param s the associated schedule
     */
    public void populate( Person p, Schedule s ) {
	people.add( new PersonSchedule( p, s ) );
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
	    for (PersonSchedule ps: pk.people) {
		ps.person.emplace( pk.findPlace(), ps.schedule );
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

/** Tuple of start and end times used for scheduling people's visits to places
 */
class Schedule {
    // instance variables
    public final float startTime; // times are in seconds anno midnight
    public final float endTime;

    /** tool to defer computation of messages output by methods of Schedule
     *  To pass a specific message, create a subclass of Message to do it
     *  In general, this will be used to create lambda expressions, so
     *  users will not need to even know the class name!
     */
    public interface Message {
	String myString();
    }

    /** construct a new Schedule
     *  @param in -- the input stream
     *  @param context -- the context for error messages
     *  Syntax: (0.0-0.0)
     *  Meaning: (start-end) times given in hours from midnight
     *  The begin paren must just have been scanned from the input stream
     */
    public Schedule( MyScanner in, Message context ) {

	// get start time of schedule
	final float st = in.getNextFloat(
	    23.98F, ()-> context.myString() + "(: not followed by start time"
	);
	in.getNextLiteral(
	    MyScanner.dash, ()-> context.myString() + "(" + st
						    + ": not followed by -"
	);
	// get end time of schedule
	final float et = in.getNextFloat(
	    23.99F, ()-> context.myString() + "(" + st
					    + "-: not followed by end time"
	);
	in.getNextLiteral(
	    MyScanner.endParen,
	    ()-> context.myString() + "(" + st + "-" + et
				    + ": not followed by )"
	);

	// check sanity constraints on start and end times
	if (st >= 24.00F) {
	    Error.warn(
		context.myString() + "(" + st + "-" + et
				   + "): start time is tomorrow"
	    );
	}
	Check.nonNegative( st, 0.0F,
	    ()-> context.myString() + "(" + st + "-" + et
				    + "): start time is yesterday"
	);
	if (st >= et) {
	    Error.warn(
		context.myString() + "(" + st + "-" + et
				   + "): times out of order"
	    );
	}
	startTime = st * Time.hour;
	endTime = et * Time.hour;
    }

    /** contert a Schedule back to textual form
     *  @return the schedule as a string
     *  Syntax: (0.0-0.0)
     *  Meaning: (start-end) times given in hours from midnight
     */
    public String toString() {
	return "(" + startTime/Time.hour+ "-" + endTime/Time.hour+ ")";
    }
}

/** People in the simulated community each have a role
 *  @see Person
 *  @see PlaceSchedule
 *  Roles create links from people to the categories of places they visit
 */
class Role {

    // linkage from role to associated place involves a schedule
    private class PlaceSchedule {
	public PlaceKind placeKind;
	public Schedule schedule;
	public PlaceSchedule( PlaceKind p, Schedule s ) {
	    placeKind = p;
	    schedule = s;
	}
    }

    // instance variables
    public final String name; // name of this role
    private final LinkedList<PlaceSchedule> placeKinds = new LinkedList<>();

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

	name = in.getNextName( "???", ()-> "role with no name" );
	fraction = in.getNextFloat(
	    9.9999F, ()-> "role " + name + ": not followed by population"
	);

	// get the list of places associated with this role
	boolean hasNext = in.hasNext(); // needed below for missing semicolon
	while (hasNext && !in.tryNextLiteral( MyScanner.semicolon )) {

	    String placeName = in.getNextName( "???", ()->"role with no name" );
	    PlaceKind pk = PlaceKind.findPlaceKind( placeName );
	    Schedule s = null;

	    // is placeName followed a schedule?
	    if (in.tryNextLiteral( MyScanner.beginParen )) {
		s = new Schedule( in, ()-> this.describe() + " " + placeName );
	    }

	    // was it a real place name?
	    if (pk == null) {
		Error.warn(
		    this.describe() + " " + placeName + ": undefined place?"
		);
	    }

	    // see if this role is already associated with this PlaceKind
	    boolean duplicated = false;
	    for (PlaceSchedule ps: placeKinds) {
		if (ps.placeKind == pk) duplicated = true;
	    }
	    if (duplicated) {
		Error.warn(
		    this.describe() + " " + placeName + ": place name reused?"
		);
	    } else {
		placeKinds.add( new PlaceSchedule( pk, s ) );
	    }
	    hasNext = in.hasNext();
	}
	if (!hasNext) {
	    Error.warn(
		this.describe() + ": missing semicolon?"
	    );
	}

	// complain if the name is not unique
	if (findRole( name ) != null) {
	    Error.warn( this.describe() + ": role name reused?" );
	}
	// force the fraction or population to be positive
	fraction = Check.positive( fraction, 0.0F,
	    ()-> this.describe() + ": negative population?"
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
		for (PlaceSchedule ps: r.placeKinds) {
		    ps.placeKind.populate( p, ps.schedule );
		}
	    }
	}

	// finish putting people in their places
	// this actually creates the places and puts people in them
	PlaceKind.distributePeople();
    }
}

/** Infection States 
 */
class InfectionState{
    // the lognormal distribution parameter: median scatter
    double median;
    double scatter;

    // the probability of recovering from the state
    double recoverProb; 

    // the probability of recovering from the state
    double severeProb;

    InfectionState nextState = null;

    String stateName = null;

    // static variable to store all the infections states
    private static Hashtable<String, InfectionState> states = new Hashtable<>();
    
    public InfectionState(double m, double s, double rp, double sp){
        median = m;
        scatter = s;
        recoverProb = rp;
        severeProb = sp;
    }

    public void setNextState(InfectionState infectionState){
        nextState = infectionState;
    }

    public void setName(String name){
        stateName = name;
    }
    
    public float getLogNormal(){
        double sigma = (float)Math.log( (scatter + median) / median );
        double lognormal = Math.exp( sigma * MyRandom.stream().nextGaussian() ) * median;
        return (float)lognormal;
        
    }

    public static void addStates(InfectionState infectionState){
        states.put(infectionState.stateName, infectionState);
    }

    public static InfectionState getInfectionState(String name){
        return states.get(name);
    }
}


/** People are the central actors in the simulation
 *  @see Role for the roles people play
 *  @see Place for the places people visit
 */
class Person {

    // linkage from person to place involves a schedule
    private class PlaceSchedule {
	public Place place;
	public Schedule schedule;
	public PlaceSchedule( Place p, Schedule s ) {
	    place = p;
	    schedule = s;
	}
    }

    
    // infected state, true only if the 
    private InfectionState infectionState = null;




    // instance variables
    private final Role role;      // role of this person
    private final LinkedList<PlaceSchedule> places = new LinkedList<>();

    // static variables used for all people
    private static LinkedList<Person> allPeople = new LinkedList<Person>();

    // static variables for infections
    private static int totalNumberInfected;
    private static int haveInfected;
    private static int endTime;
    

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
     *  @param s -- the associated schedule
     */
    public void emplace( Place p, Schedule s ) {
	places.add( new PlaceSchedule( p, s ) );
    }

    /** Return the infection state  
     */
    public InfectionState getInfectionState() {
        return infectionState;
    }

    /**
     * true if infection STATE IS NOT null
     * @return
     */
    public boolean isInfected(){
        return infectionState != null;
    }

    /**
     * infect randomly
     */
    public void infect(){
        if (isInfected()) return;
        double prob = 1.0f - Person.haveInfected / (Person.totalNumberInfected + 0.0f);
        MyRandom rand = MyRandom.stream();
        if (rand.nextDouble() < prob){
            infectionState = InfectionState.getInfectionState("latent");
            Simulator.schedule(0.0 + infectionState.getLogNormal(), 
                (double t)->this.beInfectious(t));
                Person.haveInfected ++;
        }
    }

    /**
     * Progross the infection 
     * @param t
     */
    public void beInfectious(double t){
        MyRandom rand = MyRandom.stream();
        double prob = infectionState.recoverProb;
        if (rand.nextDouble() < prob){
            infectionState = InfectionState.getInfectionState("recovered");
        }else{
            infectionState = infectionState.nextState;
            if (!infectionState.stateName.equals("dead")){
                Simulator.schedule(t + infectionState.getLogNormal(), 
                (double tnew)->this.beInfectious(tnew));
            }
        }
    }

    /** Print out the entire population
     *  This is needed only in the early stages of debugging
     *  and obviously useless for large populations
     */
    public static void printAll() {
	for (Person p: allPeople) {
	    // line 1: person id and role
	    System.out.print( p.toString() );
	    System.out.print( " " );
	    System.out.println( p.role.name );

	    // lines 2 and up: each place and its schedule
	    for (PlaceSchedule ps: p.places ) {
		System.out.print( " " ); // indent following lines
		System.out.print( ps.place.kind.name );
		System.out.print( " " );
		System.out.print( ps.place.toString() );
		if (ps.schedule != null) {
		    System.out.print( ps.schedule.toString() );
		}
		System.out.println();
	    }
	}
    }

    /**
     * read the input file
     * @param in
     */
    public static void infectionSetUp(MyScanner in){

        // read infected
        totalNumberInfected = in.getNextInt(0, ()->"infectionSetUp: missing integer");
        in.getNextLiteral(
            MyScanner.semicolon,
            ()->"infectionSetUp missing semicolon"
	    );

        // read latent
        float scatter, median, recoverProb;
        in.getNextLiteral(Pattern.compile("latent"), ()->"infectionSetUp: should be latent");

        median = in.getNextFloat(0.0f, ()->"infectionSetUp latent: invalid median");
        scatter = in.getNextFloat(0.0f, ()->"infectionSetUp latent: invalid scatter");

        InfectionState latent = new InfectionState(median, scatter, 0.0f, 1.0);
        latent.setName("latent");
        InfectionState.addStates(latent);

        in.getNextLiteral(
            MyScanner.semicolon,
            ()->"infectionSetUp missing semicolon"
	    );

        //  read asymptomatic
        in.getNextLiteral(Pattern.compile("asymptomatic"), ()->"infectionSetUp: should be asymptomatic");

        median = in.getNextFloat(0.0f, ()->"infectionSetUp asymptomatic: invalid median");
        scatter = in.getNextFloat(0.0f, ()->"infectionSetUp asymptomatic: invalid scatter");

        InfectionState asymptomatic = new InfectionState(median, scatter, 0.0f, 1.0);
        asymptomatic.setName("asymptomatic");
        InfectionState.addStates(asymptomatic);
        in.getNextLiteral(
            MyScanner.semicolon,
            ()->"infectionSetUp missing semicolon"
	    );

        // read symptomatic
        in.getNextLiteral(Pattern.compile("symptomatic"), ()->"infectionSetUp: should be symptomatic");

        median = in.getNextFloat(0.0f, ()->"infectionSetUp symptomatic: invalid median");
        scatter = in.getNextFloat(0.0f, ()->"infectionSetUp symptomatic: invalid scatter");
        recoverProb = in.getNextFloat(0.0f, ()->"infectionSetUp symptomatic: invalid recover probability");

        InfectionState symptomatic = new InfectionState(median, scatter, recoverProb, 1.0 - recoverProb);
        symptomatic.setName("symptomatic");
        InfectionState.addStates(symptomatic);

        in.getNextLiteral(
            MyScanner.semicolon,
            ()->"infectionSetUp missing semicolon"
	    );
        
        // read bedridden
        in.getNextLiteral(Pattern.compile("bedridden"), ()->"infectionSetUp: should be bedridden");

        median = in.getNextFloat(0.0f, ()->"infectionSetUp bedridden: invalid median");
        scatter = in.getNextFloat(0.0f, ()->"infectionSetUp bedridden: invalid scatter");
        recoverProb = in.getNextFloat(0.0f, ()->"infectionSetUp bedridden: invalid recover probability");
        
        InfectionState bedridden = new InfectionState(median, scatter, recoverProb, 1.0 - recoverProb);
        bedridden.setName("bedridden");
        InfectionState.addStates(bedridden);
        in.getNextLiteral(
            MyScanner.semicolon,
            ()->"infectionSetUp missing semicolon"
	    );

        in.getNextLiteral(Pattern.compile("end"), ()->"infectionSetUp: should be end");

        endTime = in.getNextInt(0, ()->"infectionSetUp: interger");

        in.getNextLiteral(
            MyScanner.semicolon,
            ()->"infectionSetUp missing semicolon"
	    );

        // connect states
        InfectionState recovered = new InfectionState(0, 0, 0, 0);
        recovered.setName("recovered");
        InfectionState.addStates(recovered);

        InfectionState dead = new InfectionState(0, 0, 0, 0);
        dead.setName("dead");
        InfectionState.addStates(dead);
        
        latent.nextState = asymptomatic;
        asymptomatic.nextState = symptomatic;
        symptomatic.nextState = bedridden;
        bedridden.nextState = dead;
        
    }

    public static void printDailyReport(double t){
        int latent = 0;
        int asy = 0;
        int sym = 0;
        int bedridden = 0;
        int recovered = 0;
        int dead = 0;
        int uninfected = 0;
        for (Person person : allPeople) {
            if (!person.isInfected()){
                 uninfected ++;
            }else if (person.getInfectionState().stateName.equals("latent")){
                latent ++;
            }else if (person.getInfectionState().stateName.equals("asymptomatic")){
                asy++;
            }else if (person.getInfectionState().stateName.equals("symptomatic")){
                sym ++;
            }else if (person.getInfectionState().stateName.equals("bedridden")){
                bedridden ++;
            }else if (person.getInfectionState().stateName.equals("recovered")){
                recovered ++;
            }else if (person.getInfectionState().stateName.equals("dead")){
                dead ++;
            }else{
                uninfected ++;
            }
        }
        System.out.printf("%.1f,%d,%d,%d,%d,%d,%d,%d\n",
            t, uninfected, latent, asy, sym, bedridden, recovered, dead);
    }

    public static void simulate(){
        // first infect some people
        while (haveInfected < totalNumberInfected){
            for (Person person : allPeople) {
                person.infect();
            }
        }

        System.out.println("time,uninfected,latent,asymptomatic,symptomatic,bedridden,recovered,dead");
        for (int i = 0; i <= endTime; i++) {
            Simulator.schedule(i, (double t)->printDailyReport(t));
        }

        // add 0.0001 to avoid report missing
        Simulator.schedule(endTime + 0.0001, (double t)->System.exit(0));
    }


}


/** Framework for discrete event simulation
 */
class Simulator {
    private Simulator() {} // prevent construction of instances!  Don't call!

    /** Functional interface for scheduling actions to be done later
     *  Users will generally never mention Action or trigger because
     *  this is used to support lambda expressions passed to schedule().
     */
    public static interface Action {
	void trigger( double time );
    }

    private static class Event {
	public final double time; // when will this event occur
	public final Action act;  // what to do then
	public Event( double t, Action a ) {
	    time = t;
	    act = a;
        }
    }

    private static final PriorityQueue<Event> eventSet = new PriorityQueue<>(
	( Event e1, Event e2 )-> Double.compare( e1.time, e2.time )
    );

    /** Schedule an event to occur at a future time
     *  @param t, the time of the event
     *  @param a, what to do for that event
     *  example:
     *  <pre>
     *    Simulator.schedule( now+later, (double t)-> whatToDo( then, stuff ) );
     *  </pre>
     */
    public static void schedule( double t, Action a ) {
	eventSet.add( new Event( t, a ) );
    }

    /** Run the simulation
     *  Before running the simulation, schedule the initial events
     *  all of the simulation occurs as side effects of scheduled events
     */
    public static void run() {
	while (!eventSet.isEmpty()) {
	    Event e = eventSet.remove();
	    e.act.trigger( e.time );
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
	    String keyword = in.getNextName( "???", ()-> "keyword expected" );
	    if ("population".equals( keyword )) {
		// get population, semicolon
		final int p = in.getNextInt( 1,
		    ()-> "population: missing integer"
		);
		in.getNextLiteral(
		    MyScanner.semicolon,
		    ()-> "population " + p + ": missing ;"
		);

		// sanity constraints on population
		if (pop != 0) {
		    Error.warn( "population specified more than once" );
		} else {
		    pop = p;
		}
		if (pop <= 0) {
		    Error.warn( "population " + p + ": not positive" );
		    pop = 1;
		}
	    } else if ("role".equals( keyword )) {
		new Role( in );
	    } else if ("place".equals( keyword )) {
		new PlaceKind( in );
	    } else if ("infected".equals(keyword)){
        Person.infectionSetUp(in);
        }else if (keyword == "???") { // there was no keyword
		// == is allowed here 'cause we're detecting the default value
		// we need to advance the scanner here or we'd stick in a loop
		if (in.hasNext()) in.next();
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
	    //Person.printAll(); // BUG:  In the long run, this is just for debug

        // setup simulation
        Person.simulate();
        // run simulation
        Simulator.run();

	} catch ( FileNotFoundException e ) {
	    Error.fatal( "could not open file: " + args[0] );
	}
    }
}