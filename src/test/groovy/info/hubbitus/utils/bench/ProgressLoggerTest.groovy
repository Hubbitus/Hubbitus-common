package info.hubbitus.utils.bench

import groovyx.gpars.GParsPool
import spock.lang.Specification

import java.util.function.Supplier

/**
 * @author Pavel Alexeev.
 * @created 2016-11-12 13:24.
 */
class ProgressLoggerTest extends Specification {
	List digitsList = [1, 2, 3, 4, 5]

	List objectsList = ['one', 'two', 'three', 'four']

	final StringBuffer sb = new StringBuffer()
	Closure bufferWrite = { synchronized (sb) { sb.append(it).append('\n') } }

	final List resList = []
	Closure listWrite = { synchronized (resList) {resList.add(it)} }

	public static String digitsListResultRegexp = /Process \[1\] #1 from 5 \(20[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[2\] #2 from 5 \(40[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\). Estimated items: 3, time: \d+[,.]\d{3}
Process \[3\] #3 from 5 \(60[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\). Estimated items: 2, time: \d+[,.]\d{3}
Process \[4\] #4 from 5 \(80[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\). Estimated items: 1, time: \d+[,.]\d{3}
Process \[5\] #5 from 5 \(100[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
/

	public static String digitsListObjectClassNameResultRegexp = /Process \[Integer\] #1 from 5 \(20[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[Integer\] #2 from 5 \(40[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\). Estimated items: 3, time: \d+[,.]\d{3}
Process \[Integer\] #3 from 5 \(60[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\). Estimated items: 2, time: \d+[,.]\d{3}
Process \[Integer\] #4 from 5 \(80[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\). Estimated items: 1, time: \d+[,.]\d{3}
Process \[Integer\] #5 from 5 \(100[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
/

	/**
	 * Stream opposite to list does not known amount of elements on start
	 */
	public static String digitsStreamResultRegexpUnknownTotalAmount = /Process \[item\] #1\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[item\] #2\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[item\] #3\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[item\] #4\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[item\] #5\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
/

	public static String objectsStreamResultRegexpWithUnknownTotalAmount = /Process \[one\] #1\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[two\] #2\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[three\] #3\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[four\] #4\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
/

	public static String objectsStreamResultRegexp = /Process \[one\] #1 from 4 \(25[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
Process \[two\] #2 from 4 \(50[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)\. Estimated items: 2, time: \d+[,.]\d{3}
Process \[three\] #3 from 4 \(75[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)\. Estimated items: 1, time: \d+[,.]\d{3}
Process \[four\] #4 from 4 \(100[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)
/

	def "each: static simple call"() {
		when:
			ProgressLogger.each(digitsList){
				println it // Some long run operation
			}{
				sb.append(it).append('\n')
			}
		then:
			sb.length() > 0
			sb ==~ digitsListResultRegexp
	}

	def "next: default variant. Java-style loop"() {
		given:
			ProgressLogger pl = new ProgressLogger(digitsList, bufferWrite);

		when:
			for (Object obj in digitsList){
				pl.next()
			};
		then:
			sb ==~ digitsListObjectClassNameResultRegexp
	}

	def "next: unknown amount of elements"() {
		given:
			ProgressLogger pl = new ProgressLogger(-1, bufferWrite);

		when:
			digitsList.each{
				sleep new Random().nextInt(100)
				pl.next()
			};
		then:
			sb ==~ digitsStreamResultRegexpUnknownTotalAmount
	}

	def "next: unknown amount of elements, call next with Closure argument"() {
		given:
			ProgressLogger pl = new ProgressLogger(-1, bufferWrite);

		when:
			digitsList.each{
				pl.next { sleep new Random().nextInt(100) }
			};
		then:
			sb ==~ digitsStreamResultRegexpUnknownTotalAmount
	}

	def "next: unknown amount of elements, call next with Lambda argument"() {
		given:
			ProgressLogger pl = new ProgressLogger(-1, bufferWrite);

		when:
			digitsList.each{
				pl.next(
					new Supplier() {
						@Override
						Object get() {
							return sleep (new Random().nextInt(100))
						}
					}
				)
			};
		then:
			sb ==~ digitsStreamResultRegexpUnknownTotalAmount
	}

	def "next: unknown amount of elements, call next with Closure argument, Groovy Map-stype constructor"() {
		given:
			ProgressLogger pl = new ProgressLogger(outMethod: bufferWrite);

		when:
			digitsList.each{
				pl.next { sleep new Random().nextInt(100) }
			};
		then:
			sb ==~ digitsStreamResultRegexpUnknownTotalAmount
	}

	def "next: unknown amount of elements, call next with String argument"() {
		given:
			ProgressLogger pl = new ProgressLogger(-1, bufferWrite)

		when:
			objectsList.each{
				pl.next it
			};
		then:
			sb ==~ objectsStreamResultRegexpWithUnknownTotalAmount
	}

	def "next: unknown amount of elements, call next with Closure argument, Groovy Map-stype constructor, alternative processName (string prefix)"() {
		given:
			ProgressLogger pl = new ProgressLogger(outMethod: bufferWrite, processName: 'Run test');

		when:
			objectsList.each{
				pl.next it
				sleep new Random().nextInt(100)
			}
		then:
			sb ==~ objectsStreamResultRegexpWithUnknownTotalAmount.replaceAll('Process ', 'Run test ')
	}

	def "measure: simple test"() {
		when:
			ProgressLogger.measure(bufferWrite, { println 'Some heavy work'; '' }, 'Doing cool work!')
		then:
			sb.length() > 0
			sb ==~ /Stop processing \[Doing cool work!\] \(Total processed 1\)\. Spent: 0[,.]\d{3}\.\n/
	}

	def 'measure() with object result from closure'(){
		when:
			ProgressLogger.measure(bufferWrite, { return new Expando(one: 1, two: 2) }, 'Test measure')
		then:
			noExceptionThrown()
	}

	def "next: iterate by collection (fixed amount of elements)"() {
		given:
			ProgressLogger pl = new ProgressLogger(objectsList, bufferWrite);

		when:
			objectsList.each{
				pl.next it
			}
		then:
			sb ==~ objectsStreamResultRegexp
	}

	/**
	 * With multi-threading environment you should provide synchronized outMethod and results may be in unpredictable
	 * order of iteration completeness like:
	 * Process [Integer] #10 from 10 (100,00%). Spent (pack by 1) time: 0,382 (from start: 0,386)
	 * Process [Integer] #1 from 10 (10,00%). Spent (pack by 1) time: 0,382 (from start: 0,386)
	 * Process [Integer] #6 from 10 (60,00%). Spent (pack by 1) time: 0,382 (from start: 0,386). Estimated items: 4, time: 0,257
	 * Process [Integer] #8 from 10 (80,00%). Spent (pack by 1) time: 0,382 (from start: 0,386). Estimated items: 2, time: 0,096
	 * Process [Integer] #2 from 10 (20,00%). Spent (pack by 1) time: 0,382 (from start: 0,386). Estimated items: 8, time: 1,543
	 * Process [Integer] #5 from 10 (50,00%). Spent (pack by 1) time: 0,382 (from start: 0,386). Estimated items: 5, time: 0,386
	 * Process [Integer] #3 from 10 (30,00%). Spent (pack by 1) time: 0,382 (from start: 0,386). Estimated items: 7, time: 0,900
	 * Process [Integer] #4 from 10 (40,00%). Spent (pack by 1) time: 0,382 (from start: 0,386). Estimated items: 6, time: 0,579
	 * Process [Integer] #9 from 10 (90,00%). Spent (pack by 1) time: 0,382 (from start: 0,386). Estimated items: 1, time: 0,043
	 * Process [Integer] #7 from 10 (70,00%). Spent (pack by 1) time: 0,382 (from start: 0,386). Estimated items: 3, time: 0,165
	 * So we should check start and end corner cases and also what all numbers present in log!
	 * @return
	 */
	def "next: Parallel, multi-threading! iterate by collection (fixed amount of elements)"() {
		given:
			int N = 100
			List list = (1..N).toList()
			ProgressLogger pl = new ProgressLogger(list, listWrite, 'Integer', 1)

		when:
			GParsPool.withPool(10){
				list.eachParallel {
					pl.next {
						sleep 10
					}
				}
			}
		then:
			// First (please note lines unordered!)
			resList.find {
				it ==~ /^Process \[Integer] #1 from 100 \(1[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)\Z/
			}
			// Last
			resList.find{
				it ==~ /^Process \[Integer] #100 from 100 \(100[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)\Z/
			}

			(list - [1, N]).each{n-> // All middle lines
				assert resList.find{
					it ==~ /^Process \[Integer\] #${n} from ${N} \(${n}[,.]00%\)\. Spent \(pack by 1\) time: \d+[,.]\d{3} \(from start: \d+[,.]\d{3}\)\. Estimated items: ${N - n}, time: \d+[,.]\d{3}\Z/
				}
			}
	}

	def measureAndLogTimeTest(){
		when:
			def result = ProgressLogger.measureAndLogTime({spent-> bufferWrite('Operation took: ' + spent) }){
				println 'test' // Some long measured work
				return 42
			}
		then:
			42 == result
			sb ==~ /Operation took: 0[,.]\d{3}\n/
	}
}
