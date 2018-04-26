package info.hubbitus.utils.bench

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.math.RoundingMode

/**
 * Class to represent lastPackSpentNs time including formatting.
 *
 * Class before was inner for ProgressLogger, but due to the Groovy bugs in static inner classes:
 * http://jira.codehaus.org/browse/GROOVY-4287
 * http://jira.codehaus.org/browse/GROOVY-4683
 * and appearing new functionality like benchmarking it split out into separate class.
 *
 * Born in ais adapter, added refactored.
 *
 * @autor: Pavel Alexeev <Pahan@Hubbitus.info>
 * @created: 2013-03-03 11:40:48
 * @imported: 04.01.2015 00:13:29
 */
@CompileStatic
public class Spent implements Comparable<Spent>{
	long spent // Nanoseconds
	def info

	@Lazy private String spentStr = formatTimeElapsedSinceNanosecond(spent);

	/**
	 * Do not format anything if it less than 1 minute by default
	 * Nanoseconds.
	 */
	static long MINIMUM_SPENT_FOR_FORMATTING = 60000

	/**
	 * Constructor from amount of nanoseconds
	 *
	 * @param spent
	 */
	public Spent(long spent){
		this.spent = spent
	}

	/**
	 * Format nanoseconds to elapsed time format
	 * Prototype got from: http://stackoverflow.com/questions/567659/calculate-elapsed-time-in-java-groovy
	 *
	 * @param time difference in nanoseconds
	 * @return Human readable string representation - eg. 2 days, 14 hours, 5 minutes
	 */
	public static String formatTimeElapsedSinceNanosecond(long nanosDiff) {

		if(nanosDiff / (10 ** 6) <= MINIMUM_SPENT_FOR_FORMATTING){ return sprintf('%.3f', (nanosDiff / (10 ** 9)).setScale(3, RoundingMode.HALF_UP)) }

		String formattedTime = ''
		long secondInNanos = (long)(10 ** 9) // Explicit casting only for @CompileStatic
		long minuteInNanos = secondInNanos * 60
		long hourInNanos = minuteInNanos * 60
		long dayInNanos = hourInNanos * 24
		long weekInNanos = dayInNanos * 7
		long monthInNanos = dayInNanos * 30

		List timeElapsed = []
		// Define time units - plural cases are handled inside loop
		List timeElapsedText = [
			'secs', 'mins', 'hours', 'days', 'weeks', 'months' // @TODO add formatting and/or internationalization support
		]

		timeElapsed[5] = (int) (nanosDiff / monthInNanos)	// months
		nanosDiff %= monthInNanos;
		timeElapsed[4] = (int) (nanosDiff / weekInNanos)	// weeks
		nanosDiff %= weekInNanos
		timeElapsed[3] = (int) (nanosDiff / dayInNanos)	// days
		nanosDiff %= dayInNanos
		timeElapsed[2] = (int) (nanosDiff / hourInNanos)	// hours
		nanosDiff %= hourInNanos
		timeElapsed[1] = (int) (nanosDiff / minuteInNanos)	// minutes
		nanosDiff %= minuteInNanos
		timeElapsed[0] = sprintf('%.3f', (nanosDiff / secondInNanos).setScale(3, RoundingMode.HALF_UP))	// seconds

		// Only adds 3 significant high valued units
		int i = (timeElapsed.size()-1);
		for(int j=0; i>=0 && j<3; i--){
			// loop from high to low time unit
			if(timeElapsed[i] != 0){
				formattedTime += ((j>0)? ", " :"") \
					+ timeElapsed[i] \
					+ " " + timeElapsedText[i]
				++j
			}
		} // end for - build string

		return formattedTime
	}

	public Spent plus(Spent other){
		this.spent += other.spent
		return this
	}

	public String toString(){
		return spentStr + (info ? "\n$info" : '')
	}

	@Override
	int compareTo(Spent other) {
		(other.spent <=> this.spent)
	}
}