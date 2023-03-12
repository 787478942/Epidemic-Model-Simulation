/* LogNormal.java
 * Demonstration of how to generate a log-normal probability distribution.
 * This reads the median and scatter as command line arguments, and then
 * generates a histogram of the distribution over 100 trials on the interval
 * from 0 to 19, with bins 0 and 19 holding results outside that range.
 *
 *  author Douglas W. Jones
 *  version Sept. 25, 2020
 */

import java.util.Random;
import java.lang.Math;
import java.lang.NumberFormatException;

class LogNormal {

    public static void main( String arg[] ) {
        Random rand = new Random();  // a source of random numbers
        double median = 0.0;         // the median of the log normal distr
        double scatter = 0.0;        // the scatter of the distribution

	// the histogram accumulator
	final int bound = 20;
	int [] histogram = new int[bound];

	// get median and scatter from the command line
	if (arg.length != 2) {
	    System.err.println( "2 arguments required -- median and scatter" );
	    System.exit( 1 );
	} else try {
	    median = Double.parseDouble( arg[0] );
	    scatter = Double.parseDouble( arg[1] );
	} catch (NumberFormatException e) {
	    System.err.println(
		"non numeric argument " + arg[0] + " " + arg[1]
	    );
	    System.exit( 1 );
        }

	// check median and scatter for legitimacy
	if (median <= 0.0) {
	    System.err.println( "median " + arg[0] + " must be positive" );
	    System.exit( 1 );
        }
	if (scatter < 0.0) {
	    System.err.println( "scatter " + arg[1] + " must not be negative" );
	    System.exit( 1 );
        }

	// output heading for the histogram
	System.out.println( "median " + median + ", scatter " + scatter );

	// sigma is the standard deviation of the underlying normal distribution
	double sigma = Math.log( (scatter + median) / median );

	// do the experiment to generate the histogram
	for (int i = 0; i < 100; i++) {

	    // draw a random number from a log normal distribution
	    double lognormal = Math.exp( sigma * rand.nextGaussian() ) * median;

	    // find what bin of the histogram it goes in and increment that bin
	    int bin = (int)Math.ceil( lognormal );
	    if (bin <= 0) bin = 0;
	    if (bin >= bound) bin = bound - 1;
	    histogram[bin]++;
	}

	// print the histogram
	for (int i = 0; i < bound; i++) {
	    System.out.printf( " %2d", i );
	    for (int j = 0; j < histogram[i]; j++ ) System.out.print( 'X' );
	    System.out.print( '\n' );
	}
    }
}
