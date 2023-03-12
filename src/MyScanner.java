import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Support for scanning input files with error reporting
 * 
 * @author Douglas W. Jones
 * @version Apr. 6, 2021 lifted from Epidemic.java of that date
 * @see Error
 * @see java.util.Scanner Ideally, this would be extend class Scanner, but class
 *      Scanner is final Therefore, this is a wrapper class around class Scanner
 */
public class MyScanner {
	private Scanner sc; // the scanner we are wrapping

	public MyScanner(File f) throws FileNotFoundException {
		sc = new Scanner(f);
	}

	// methods that we wish we could inhereit from Scanner
	public boolean hasNext() {
		return sc.hasNext();
	}

	public boolean hasNext(String s) {
		return sc.hasNext(s);
	}

	public String next() {
		return sc.next();
	}

	// patterns that matter here

	// delimiters are spaces, tabs, newlines and carriage returns
	private static final Pattern delimPat = Pattern.compile("[ \t\n\r]*");

	// note that all of the following patterns allow an empty string to match
	// this is used in error detection below

	// if it's not a name, it begins with a non-letter
	private static final Pattern NotNamePat = Pattern.compile("([^A-Za-z]*)|");

	// names consist of a letter followed optionally by letters or digits
	private static final Pattern namePat = Pattern.compile("([A-Za-z][0-9A-Za-z]*)|");

	// if it's not an int, it begins with a non-digit, non-negative-sign
	private static final Pattern NotIntPat = Pattern.compile("([^-0-9]*)|");

	// ints consist of an optional sign followed by at least one digit
	private static final Pattern intPat = Pattern.compile("((-[0-9]|)[0-9]*)");

	// floats consist of an optional sign followed by
	// at least one digit, with an optional point before between or after them
	private static final Pattern floatPat = Pattern.compile("-?(([0-9]+\\.[0-9]*)|(\\.[0-9]+)|([0-9]*))");

	/**
	 * tool to defer computation of messages output by methods of MyScanner To pass
	 * a specific message, create a subclass of Message to do it In general, this
	 * will be used to create lambda expressions, so users will not need to even
	 * know the class name!
	 */
	public interface Message {
		String myString();
	}

	// new methods added to class Scanner

	/**
	 * get the next nae from the scanner or complain if missing See namePat for the
	 * details of what makes a float.
	 * 
	 * @param defalt      -- return value if there is no next item
	 * @param errorMesage -- the message to complain with (lambda expression)
	 * @return the next item or the defalt
	 */
	public String getNextName(String defalt, Message errorMessage) {
		// first skip the delimiter, accumulate anything that's not a name
		String notName = sc.skip(delimPat).skip(NotNamePat).match().group();

		// second accumulate the name
		String name = sc.skip(namePat).match().group();

		if (!notName.isEmpty()) { // there's something else a name belonged
			Error.warn(errorMessage.myString() + ": name expected, skipping " + notName);
		}

		if (name.isEmpty()) { // missing name
			Error.warn(errorMessage.myString());
			return defalt;
		} else { // there was a name
			return name;
		}
	}

	/**
	 * get the next integer from the scanner or complain if missing See intPat for
	 * the details of what makes a float.
	 * 
	 * @param defalt      -- return value if there is no next integer
	 * @param errorMesage -- the message to complain with (lambda expression)
	 * @return the next integer or the defalt
	 */
	public int getNextInt(int defalt, Message errorMessage) {
		// first skip the delimiter, accumulate anything that's not an int
		String notInt = sc.skip(delimPat).skip(NotIntPat).match().group();

		// second accumulate the int, if any
		String text = sc.skip(delimPat).skip(intPat).match().group();

		if (!notInt.isEmpty()) { // there's something else where an int belonged
			Error.warn(errorMessage.myString() + ": int expected, skipping " + notInt);
		}

		if (text.isEmpty()) { // missing name
			Error.warn(errorMessage.myString());
			return defalt;
		} else { // the name was present and it matches intPat
			return Integer.parseInt(text);
		}
	}

	/**
	 * get the next float(double) from the scanner or complain if missing See
	 * floatPat for the details of what makes a float.
	 * 
	 * @param defalt      -- return value if there is no next integer
	 * @param defalt      -- return value if there is no next float
	 * @param errorMesage -- the message to complain with (lambda expression)
	 * @return the next float or the defalt
	 */
	public double getNextFloat(double defalt, Message errorMessage) {
		// skip the delimiter, if any, then the float, if any; get the latter
		String text = sc.skip(delimPat).skip(floatPat).match().group();

		if (text.isEmpty()) { // missing name
			Error.warn(errorMessage.myString());
			return defalt;
		} else { // the name was present and it matches intPat
			return Float.parseFloat(text);
		}
	}

	// patterns for use with the NextLiteral routines
	public static final Pattern beginParen = Pattern.compile("\\(|");
	public static final Pattern endParen = Pattern.compile("\\)|");
	public static final Pattern dash = Pattern.compile("-|");
	public static final Pattern semicolon = Pattern.compile(";|");

	/**
	 * try to get the next literal from the scanner
	 * 
	 * @param literal -- the literal to get
	 * @returns true if the literal was present and skipped, false otherwise The
	 *          literal parameter must be a pattern that can match the empty string
	 *          if the desired literal is not present.
	 */
	public boolean tryNextLiteral(Pattern literal) {
		sc.skip(delimPat); // allow delimiter before literal!
		String s = sc.skip(literal).match().group();
		return !s.isEmpty();
	}

	/**
	 * get the next literal from the scanner or complain if missing
	 * 
	 * @param literal     -- the literal to get
	 * @param errorMesage -- the message to complain with (lambda expression)
	 * @see tryNextLiteral for the mechanism used.
	 */
	public void getNextLiteral(Pattern literal, Message errorMessage) {
		if (!tryNextLiteral(literal)) {
			Error.warn(errorMessage.myString());
		}
	}
}