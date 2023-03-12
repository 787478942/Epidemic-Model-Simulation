// Schedule.java

/**
 * Tuple of start and end times used for scheduling people's visits to places
 * 
 * @author Douglas W. Jones
 * @version Apr. 10, 2021 augmented per MP10 to support likelihood of travel
 * @see Person
 * @see Place
 * @see MyScanner for the tools used to read schedules
 * @see Error for the tools used to report errors in schedules
 * @see Check for the tools used to check sanity of numbers in schedules
 * @see Simulator for the tools used to schedule activity under schedules
 * @see MyRandom for the tools used to assure randomness
 */
public class Schedule {
	// instance variables
	public final double startTime; // times are in seconds anno midnight
	public final double duration; // duration of visit
	public final double likelihood;// probability this visit will take place

	// source of randomness
	static final MyRandom rand = MyRandom.stream;

	/**
	 * construct a new Schedule
	 * 
	 * @param in      -- the input stream
	 * @param context -- the context for error messages Syntax: (0.0-0.0) Meaning:
	 *                (start-end) times given in hours from midnight The begin paren
	 *                must just have been scanned from the input stream
	 */
	public Schedule(MyScanner in, MyScanner.Message context) {

		// get start time of schedule
		final double st = in.getNextFloat(23.98F, () -> context.myString() + "(: not followed by start time");
		in.getNextLiteral(MyScanner.dash, () -> context.myString() + "(" + st + ": not followed by -");
		// get end time of schedule
		final double et = in.getNextFloat(23.99F, () -> context.myString() + "(" + st + "-: not followed by end time");

		final double lh; // likelihood of move taking place
		if (!in.tryNextLiteral(MyScanner.endParen)) {
			lh = in.getNextFloat(0.0, () -> context.myString() + "(" + st + '-' + et + "-: not followed by likelihood");

			in.getNextLiteral(MyScanner.endParen,
					() -> context.myString() + "(" + st + "-" + et + " " + lh + ": not followed by )");
		} else {
			lh = 1.0;
		}

		// check sanity constraints on schedule
		if (st >= 24.00F) {
			Error.warn(context.myString() + "(" + st + "-" + et + "): start time is tomorrow");
		}
		Check.nonNeg(st, 0.0F, () -> context.myString() + "(" + st + "-" + et + "): start time is yesterday");
		if (st >= et) {
			Error.warn(context.myString() + "(" + st + "-" + et + "): times out of order");
		}
		Check.nonNeg(lh, 0.0F,
				() -> context.myString() + "(" + st + "-" + et + " " + lh + "): likelihood cannot be negative");
		if (lh > 1.0) {
			Error.warn(context.myString() + "(" + st + "-" + et + " " + lh + "): likelihood cannot be over 1.0");
		}
		startTime = st * Time.hour;
		duration = (et * Time.hour) - startTime;
		likelihood = lh;
	}

	/**
	 * compare two schedules to see if they overlap
	 * 
	 * @return true if they overlap, false otherwise
	 */
	public boolean overlap(Schedule s) {
		if (s == null)
			return false;
		double thisEnd = this.startTime + this.duration;
		if (this.startTime <= s.startTime) {
			if (s.startTime <= (this.startTime + this.duration))
				return true;
		}
		double sEnd = s.startTime + s.duration;
		if (s.startTime <= this.startTime) {
			if (this.startTime <= (s.startTime + s.duration))
				return true;
		}
		return false;
	}

	/**
	 * commit a person to following a schedule regarding a place
	 * 
	 * @param person
	 * @param place  this starts the logical process of making a person follow this
	 *               schedule
	 */
	public void apply(Person person, Place place) {
		Simulator.schedule(startTime, (double t) -> go(t, person, place));
	}

	/**
	 * keep a person on schedule
	 * 
	 * @param person
	 * @param place  this continues a logical process of moving a person on this
	 *               schedule
	 */
	private void go(double time, Person person, Place place) {
		double tomorrow = time + Time.day;

		// first, ensure that we keep following this schedule
		Simulator.schedule(tomorrow, (double t) -> go(t, person, place));

		if (rand.nextFloat() < likelihood) {
			// second, make the person go there if they take the trip
			person.travelTo(time, place);

			// third, make sure we get home if we took the trip
			Simulator.schedule(time + duration, (double t) -> person.goHome(t));
		}
	}

	/**
	 * convert a Schedule back to textual form
	 * 
	 * @return the schedule as a string Syntax: (0.0-0.0) Meaning: (start-end) times
	 *         given in hours from midnight
	 */
	public String toString() {
		return "(" + startTime / Time.hour + "-" + (startTime + duration) / Time.hour + " " + likelihood + ")";
	}
}