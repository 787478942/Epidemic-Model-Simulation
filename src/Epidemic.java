
import java.io.File;
import java.io.FileNotFoundException;

/**
 * The main class of an epidemic simulator
 * 
 * @author Douglas W. Jones
 * @version Apr. 6, 2021 lifted from Epidemic.java of that date
 * @see MyScanner This class should never be instantiated. All methods here are
 *      static and all but the main method are private.
 * @see Role for the framework that creates people
 * @see PlaceKind for the framework from which places are constructed
 * @see Person for the ultimate result of this creation
 */
public class Epidemic {

	/**
	 * Read the details of the model from an input stream
	 * 
	 * @param in -- the stream Identifies the keywords population, role, etc and
	 *           farms out the work for most of these to the classes that construct
	 *           model parts. The exception (for now) is the total population.
	 */
	private static void buildModel(MyScanner in) {
		int pop = 0; // the population of the model, 0 = uninitialized
		int infected = 0; // number initially infected, 0 = uninitialized
		double endOfTime = 0.0; // 0.0 = uninitialized

		// rules describing the progress of the infection
		InfectionRule latent = null;
		InfectionRule asymptomatic = null;
		InfectionRule symptomatic = null;
		InfectionRule bedridden = null;

		while (in.hasNext()) { // scan the input file

			// each item begins with a keyword
			String keyword = in.getNextName("???", () -> "keyword expected");
			if ("population".equals(keyword)) {
				int p = Check.posIntSemicolon(in, () -> "population");
				if (pop != 0) {
					Error.warn("population specified more than once");
				} else {
					pop = p;
				}
			} else if ("infected".equals(keyword)) {
				int i = Check.posIntSemicolon(in, () -> "infected");
				if (infected != 0) {
					Error.warn("infected specified more than once");
				} else {
					infected = i;
				}
			} else if ("latent".equals(keyword)) {
				if (latent != null) {
					Error.warn("latency time specified more than once");
				}
				latent = new InfectionRule(in, () -> "latent");
			} else if ("asymptomatic".equals(keyword)) {
				if (asymptomatic != null) {
					Error.warn("asymptomatic time specified more than once");
				}
				asymptomatic = new InfectionRule(in, () -> "asymptomatic");
			} else if ("symptomatic".equals(keyword)) {
				if (symptomatic != null) {
					Error.warn("symptomatic time specified more than once");
				}
				symptomatic = new InfectionRule(in, () -> "symptomatic");
			} else if ("bedridden".equals(keyword)) {
				if (bedridden != null) {
					Error.warn("bedridden time specified more than once");
				}
				bedridden = new InfectionRule(in, () -> "bedridden");
			} else if ("end".equals(keyword)) {
				final double et = in.getNextFloat(1.0F, () -> "time: end time missing");
				in.getNextLiteral(MyScanner.semicolon, () -> "end " + et + ": missing ;");
				Check.positive(et, 0.0F, () -> "end " + et + ": negative end time?");
				if (endOfTime > 0.0) {
					Error.warn("end " + et + ": duplicate end time");
				} else {
					endOfTime = et;
				}
			} else if ("role".equals(keyword)) {
				new Role(in);
			} else if ("place".equals(keyword)) {
				new PlaceKind(in);
			} else if (keyword == "???") { // there was no keyword
				// == is allowed here 'cause we're detecting the default value
				// we need to advance the scanner here or we'd stick in a loop
				if (in.hasNext())
					in.next();
			} else { // none of the above
				Error.warn("not a keyword: " + keyword);
			}
		}

		// check that all required fields are filled in

		if (pop == 0)
			Error.warn("population not given");
		if (latent == null)
			Error.warn("latency time not given");
		if (asymptomatic == null)
			Error.warn("asymptomatic time not given");
		if (symptomatic == null)
			Error.warn("symptomatic time not given");
		if (bedridden == null)
			Error.warn("bedridden time not given");
		if (endOfTime == 0.0)
			Error.warn("end of time not given");

		Error.exitIfWarnings("Aborted due to errors in input");

		Person.setDiseaseParameters(latent, asymptomatic, symptomatic, bedridden);

		Simulator.schedule( // schedule the end of time
				endOfTime * Time.day, (double t) -> System.exit(0));

		// Role is responsible for figuring out how many people per role
		Role.populateRoles(pop, infected);

		// Schedule the first of the daily reports to be printed
		Simulator.schedule(0.0, (double t) -> Person.report(t));
	}

	/**
	 * The main method
	 * 
	 * @param args -- the command line arguments Most of this code is entirely about
	 *             command line argument processing. It calls buildModel and will
	 *             eventuall also start the simulation.
	 */
	public static void main(String[] args) {
		if (args.length < 1)
			Error.fatal("missing file name");
		if (args.length > 1)
			Error.warn("too many arguments: " + args[1]);
		try {
			buildModel(new MyScanner(new File(args[0])));
			// Person.printAll(); // BUG: potentially useful for debugging
			Simulator.run();
		} catch (FileNotFoundException e) {
			Error.fatal("could not open file: " + args[0]);
		}
	}
}
