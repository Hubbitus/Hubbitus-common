package info.hubbitus.utils.bench

import spock.lang.Specification

/**
 * @author Pavel Alexeev.
 * @created 2016-11-12 13:24.
 */
class ProgressLoggerTest extends Specification {
	List digitsList = [1, 2, 3, 4, 5]

	List objectsList = ['one', 'two', 'three', 'four']

	StringBuffer sb = new StringBuffer()

	Closure bufferWrite = {sb.append(it).append('\n')}

	public static String digitsListResultRegexp = /Process \[Integer\] #1 from 5 \(20,00%\)\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
Process \[Integer\] #2 from 5 \(40,00%\)\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\). Estimated items: 3, time: \d+,\d{3}
Process \[Integer\] #3 from 5 \(60,00%\)\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\). Estimated items: 2, time: \d+,\d{3}
Process \[Integer\] #4 from 5 \(80,00%\)\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\). Estimated items: 1, time: \d+,\d{3}
Process \[Integer\] #5 from 5 \(100,00%\)\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
/

	/**
	 * Stream opposite to list does not known amount of elements on start
	 */
	public static String digitsStreamResultRegexp = /Process \[item\] #1\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
Process \[item\] #2\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
Process \[item\] #3\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
Process \[item\] #4\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
Process \[item\] #5\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
/

	public static String objectsStreamResultRegexp = /Process \[one\] #1\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
Process \[two\] #2\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
Process \[three\] #3\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
Process \[four\] #4\. Spent \(pack 1 elements\) time: \d+,\d{3} \(from start: \d+,\d{3}\)
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
			sb ==~ digitsListResultRegexp
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
			sb ==~ digitsStreamResultRegexp
	}

	def "next: unknown amount of elements, call next with Closure argument"() {
		given:
			ProgressLogger pl = new ProgressLogger(-1, bufferWrite);

		when:
			digitsList.each{
				pl.next { sleep new Random().nextInt(100) }
			};
		then:
			sb ==~ digitsStreamResultRegexp
	}

	def "next: unknown amount of elements, call next with Closure argument, Groovy Map-stype constructor"() {
		given:
			ProgressLogger pl = new ProgressLogger(outMethod: bufferWrite);

		when:
			digitsList.each{
				pl.next { sleep new Random().nextInt(100) }
			};
		then:
			sb ==~ digitsStreamResultRegexp
	}

	def "next: unknown amount of elements, call next with String argument"() {
		given:
			ProgressLogger pl = new ProgressLogger(-1, bufferWrite);

		when:
			objectsList.each{
				pl.next it
			};
		then:
			sb ==~ objectsStreamResultRegexp
	}

	def "next: unknown amount of elements, call next with Closure argument, Groovy Map-stype constructor, alternative processName (string prefix)"() {
		given:
			ProgressLogger pl = new ProgressLogger(outMethod: bufferWrite, processName: 'Run test');

		when:
			objectsList.each{
				pl.next it
				sleep new Random().nextInt(100)
			};
		then:
			sb ==~ objectsStreamResultRegexp.replaceAll('Process ', 'Run test ')
	}

	def "measure: simple test"() {
		when:
			ProgressLogger.measure(bufferWrite, { println 'Some heavy work' }, 'Doing cool work!')
		then:
			sb.length() > 0
			sb ==~ /Stop processing \[Doing cool work!\] \(Total processed 1\)\. Spent: 0,\d{3}\.\n/
	}
}
