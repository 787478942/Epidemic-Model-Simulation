// InfectionRule.java

/**
 * Statistical Description of the disease progress
 * 
 * @author Douglas W. Jones
 * @version Apr. 6, 2021 lifted from Epidemic.java of that date
 * @see MyRandom
 * @see MyScanner
 */
public class InfectionRule {
	private final double median; // median of the distribution
	private final double sigma; // sigma of the distribution
	private final double recovery; // recovery probability

	private static final MyRandom rand = MyRandom.stream();

	/**
	 * construct a new InfectionRule
	 * 
	 * @param in      -- the input stream
	 * @param context -- the context for error messages
	 */
	public InfectionRule(MyScanner in, MyScanner.Message context) {
		final double scatter;
		median = Time.day * in.getNextFloat(1.0, () -> context.myString() + ": median expected");
		scatter = Time.day * in.getNextFloat(0.0, () -> context.myString() + " " + median + ": scatter expected");
		if (!in.tryNextLiteral(MyScanner.semicolon)) {
			recovery = in.getNextFloat(0.0,
					() -> context.myString() + " " + median + " " + scatter + ": recovery probability expected");
			if (!in.tryNextLiteral(MyScanner.semicolon))
				Error.warn(context.myString() + " " + median + " " + scatter + " " + recovery + "semicolon expected");
		} else {
			recovery = 0.0;
		}

		// sanity checks on the values
		Check.positive(median, 0.0,
				() -> context.myString() + " " + median + " " + scatter + " " + recovery + ": non-positive median?");
		Check.nonNeg(scatter, 0.0,
				() -> context.myString() + " " + median + " " + scatter + " " + recovery + ": negative scatter?");
		Check.nonNeg(recovery, 0.0, () -> context.myString() + " " + median + " " + scatter + " " + recovery
				+ ": negative recovery probability?");
		if (recovery > 1.0) {
			Error.warn(context.myString() + " " + median + " " + scatter + " " + recovery
					+ ": recovery probability greater than zero?");
		}

		// we do this up front so scatter is never seen again.
		sigma = Math.log((scatter + median) / median);
	}

	/**
	 * Toss the dice to see if someone recovers under the terms of this rule
	 * 
	 * @return true if recovers, false if not
	 */
	public boolean recover() {
		return rand.nextFloat() <= recovery;
	}

	/**
	 * Toss the dice to see how long this disease state lasts under this rule
	 * 
	 * @return the time until the next change of disease state
	 */
	public double duration() {
		return rand.nextLogNormal(median, sigma);
	}
}