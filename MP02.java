// Epidemic.java
/* Program that will eventually develop into an epidemic simulator
 * author Douglas W. Jones
 * version Feb. 15, 2021
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.LinkedList;

/** Error reporting framework
 *  All error messages go to System.err (aka stderr, the standard error stream).
 *  Currently, this only supports fatal error reporting.
 *  Later it would be nice to have a way to report non-fatal errors.
 */
class Errors {

    /** Report a fatal error
     *  @param msg -- error message to be output
     *  This never returns, the program terminates reporting failure.
     */
    public static void fatal( String msg ) {
	System.err.println( "Epidemic: " + msg );
	System.exit( 1 );  // abnormal termination
    }
}

/** People in the simulated community each have a role
 */
class Role {

    // instance variables
    String name;      // name of this role
    float fraction;   // fraction of the population in this role
    int number;       // number of people in this role

    // static variables used for summary of all roles
    static float sum = 0.0F; // sum of all the fractions
    static LinkedList<Role> allRoles = new LinkedList<Role>();

    /** Construct a new role by scanning an input stream
     *  @param in -- the input stream
     *  The stream must contain the role name, and the number or fraction
     *  of the population in that role.
     *  All role specifications end with a semicolon.
     */
    public Role( Scanner in ) {

	// get the name
	if (in.hasNext()) {
	    name = in.next();
	} else {
	    Errors.fatal( "role with no name" );
	}

	// get the number in this role or the fraction of the population
	if (in.hasNextFloat()) {
	    fraction = in.nextFloat();
	    sum = sum + fraction;
	} else {
	    Errors.fatal( "role " + name + ": not followed by population" );
	}

	// get the semicolon
	if (!in.hasNext( ";" )) {
	    Errors.fatal( "role " + name + " " + fraction + ": missing ;" );
	} else {
	    in.next( ";" );
	}

        // complain if the name is not unique
        if (findRole( name ) != null) {
	    Errors.fatal(
		"role " + name + " " + fraction + ": role name reused?"
	    );
        }
	// force the fraction or population to be positive
        if (fraction <= 0) {
	    Errors.fatal(
		"role " + name + " " + fraction + ": negative population?"
	    );
	}

	allRoles.add( this ); // include this role in the list of all roles
    }

    /** Find a role, by name
     *  @param n -- the name of the role
     *  @return the role with that name, or null if none has been defined
     */
    static Role findRole( String n ) {
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
    static void populateRoles( int population ) {
	if (allRoles.isEmpty()) Errors.fatal( "no roles specified" );
	for (Role r: allRoles) {
	    // how many people are in this role
	    r.number = Math.round( (r.fraction / r.sum) * population );

	    // make that many people
	    for (int i = 1; i <= r.number; i++) new Person( r );
	}
    }
}

/** People are the central actors in the simulation
 */
class Person {
    // instance variables
    Role role;      // role of this person

    // static variables used for all people
    static LinkedList<Person> allPeople = new LinkedList<Person>();

    /** Construct a new person to perform some role
     *  @param r -- the role
     */
    public Person( Role r ) {
	role = r;

	allPeople.add( this ); // include this person in the list of all
    };

    /** Print out the entire population
     *  This is needed only in the early stages of debugging
     *  and obviously useless for large populations
     */
    public static void printAll() {
	for (Person p: allPeople) {
	    System.out.println( p.toString() + " " + p.role.name );
	}
    }
}

/** The main class
 *  This class should never be instantiated.
 *  All methods here are static and all but the main method are private.
 */
public class Epidemic {

    /** Read the details of the model from an input stream
     *  @param in -- the stream
     *  Identifies the keywords population, role, etc and farms out the
     *  work for most of these to the classes that construct model parts.
     *  The exception (for now) is the total population.
     */
    private static void buildModel( Scanner in ) {
	int pop = 0; // the population of the model, 0 = uninitialized

	while ( in.hasNext() ) { // scan the input file

	    // each item begins with a keyword
	    String keyword = in.next();
	    if ("population".equals( keyword )) {
		if (!in.hasNextInt()) {
		    Errors.fatal( "population not followed by integer" );
		} else {
		    if (pop != 0) {
			Errors.fatal( "population specified more than once" );
		    }
		    pop = in.nextInt();
		    if (pop <= 0) {
			Errors.fatal( "population " + pop + ": not positive" );
		    }
		    if (!in.hasNext( ";" )) {
			Errors.fatal( "population " + pop + ": missing ;" );
		    } else {
			in.next( ";" );
		    }
		}
	    } else if ("role".equals( keyword )) {
		new Role( in );
	    } else { // none of the above
		Errors.fatal( "not a keyword: " + keyword );
	    }
	}

	if (pop == 0) Errors.fatal( "population not specified" );
        Role.populateRoles( pop );
    }

    /** The main method
     *  @param args -- the command line arguments
     *  Most of this code is entirely about command line argument processing.
     *  It calls buildModel and will eventuall also start the simulation.
     */
    public static void main( String[] args ) {
	if (args.length < 1) Errors.fatal( "missing file name" );
	if (args.length > 1) Errors.fatal( "too many arguments: " + args[1] );
        try {
            buildModel( new Scanner( new File( args[0] ) ) );
            // BUG:  Simulate based on model just built?
            Person.printAll(); // BUG:  In the long run, this is just for debug
        } catch ( FileNotFoundException e ) {
            Errors.fatal( "could not open file: " + args[0] );
        }
    }
}
