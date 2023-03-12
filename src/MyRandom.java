// MyRandom.java

import java.util.Random;

/**
 * Wrapper extending class Random, turning it into a singleton class
 * 
 * @author Douglas W. Jones
 * @version Apr. 6, 2021 lifted from Epidemic.java of that date
 * @see Random Ideally, no user should ever create an instance of Random, all
 *      use this! Users can call MyRandom.stream.anyMethodOfRandom() (or of
 *      MyRandom) or MyRandom.stream().anyMethodOfRandom() Users can allocate
 *      MyRandom myStream = MyRandom.stream; or MyRandom myStream =
 *      MyRandom.stream(); No matter how they do it, they get the same stream
 */
public class MyRandom extends Random {
	/**
	 * the only random number stream
	 */
	public static final MyRandom stream = new MyRandom(); // the only stream;

	// nobody can construct a MyRandom except the above line of code
	private MyRandom() {
		super();
	}

	/*
	 * alternative access to the only random number stream
	 * 
	 * @return the only stream
	 */
	public static MyRandom stream() {
		return stream;
	}

	// add distributions that weren't built in

	/**
	 * exponential distribution
	 * 
	 * @param mean -- the mean value of the distribution
	 * @return a positive exponentially distributed random value
	 */
	public double nextExponential(double mean) {
		return mean * -Math.log(this.nextDouble());
	}

	/**
	 * log-normal distribution
	 * 
	 * @param median -- the median value of the distribution
	 * @param sigma  -- the sigma of the underlying normal distribution
	 * @return a log-normally distributed random value
	 */
	public double nextLogNormal(double median, double sigma) {
		return Math.exp(sigma * this.nextGaussian()) * median;
	}
}