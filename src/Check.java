/**
 * Class for semantic error checkers
 * 
 * @author Douglas W. Jones
 * @version Apr. 6, 2021 lifted from Epidemic.java of that date
 * @see Error This is a place to put error checking code that doesn't fit
 *      elsewhere. The error check methods here actually take up more space than
 *      the code they helped clarify, so the net gain in readability for this
 *      code is rather limited. Perhaps as the program grows, they'll help more.
 */
public class Check {
	private Check() {
	} // nobody should ever construct a check object

	/**
	 * Force a floating (double) value to be positive
	 * 
	 * @param v -- value to check
	 * @param d -- default value to use if the check fails
	 * @param m -- message to output if check fails
	 * @return either value if success or defalt if failure
	 */
	public static double positive(double v, double d, MyScanner.Message m) {
		if (v > 0.0) {
			return v;
		} else {
			Error.warn(m.myString());
			return d;
		}
	}

	/**
	 * Force a floating (double) value to be non negative
	 * 
	 * @param v -- value to check
	 * @param d -- default value to use if the check fails
	 * @param m -- message to output if check fails
	 * @return either value if success or defalt if failure
	 */
	public static double nonNeg(double v, double d, MyScanner.Message m) {
		if (v >= 0.0) {
			return v;
		} else {
			Error.warn(m.myString());
			return d;
		}
	}

	/**
	 * Scan end of command line containing a positive integer argument
	 * 
	 * @param in  -- the scanner to use
	 * @param msg -- the error message prefix to output if error
	 * @return the value scanned or 1 if the value was defective
	 */
	public static int posIntSemicolon(MyScanner in, MyScanner.Message msg) {
		final int num = in.getNextInt(1, () -> msg + ": missing integer");
		in.getNextLiteral(MyScanner.semicolon, () -> msg.myString() + num + ": missing ;");

		if (num <= 0) {
			Error.warn(msg.myString() + num + ": not positive");
			return 1;
		}
		return num;
	}
}