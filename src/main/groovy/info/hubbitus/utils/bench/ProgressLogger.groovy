package info.hubbitus.utils.bench

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

/**
 * Helper class logger of progress operation.
 * It is intended for easy add possibility of logging progress of operations, for example:
 * 1) in Groovy way it so simple as:
 * <code>
 *	ProgressLogger.each([1, 2, 3, 4, 5]){
 *		println it // Some long run operation
 * }
 * </code>
 * It will produce (by println) output like:
 *	Process [Integer] #1 from 5 (20,00%). Spent (pack by 1) time: 0,041 (from start: 0,047)
 *	1
 *	Process [Integer] #2 from 5 (40,00%). Spent (pack by 1) time: 0,001 (from start: 0,278). Estimated items: 3, time: 0,417
 *	2
 *	Process [Integer] #3 from 5 (60,00%). Spent (pack by 1) time: 0,012 (from start: 0,330). Estimated items: 2, time: 0,220
 *	3
 *	Process [Integer] #4 from 5 (80,00%). Spent (pack by 1) time: 0,001 (from start: 0,340). Estimated items: 1, time: 0,085
 *	4
 *	Process [Integer] #5 from 5 (100,00%). Spent (pack by 1) time: 0,001 (from start: 0,344)
 *	5
 *	Off course it will be helpful see there user defined class like Region, User or BlogPost.
 * 2) Often useful provide out method, for example to tie into current scope logger instead of global stdout, and add
 * some additional transform, it also simple:
 *	ProgressLogger.each([1, 2, 3, 4, 5]){
 *		println it
 *	}{ log.info "=$it=" }
 * 3) It may be used directly in plain old Java style like:
 *		ProgressLogger pl = new ProgressLogger(aisList, {log.info(it)});
 *		for (Object aisObj in aisList){
 *			pl.next();
 *			rowMapper(aisObj, di)
 *		}
 * 4) Or measure one run:
 *	ProgressLogger.measure({log.info(it)}, { /* long work  * / }, 'Doing cool work')
 * 5) When amount of elements or iterations is not known (f.e. stream processing or recursive calls like tree traversal)
 * totalAmountOfElements set to -1 and simpler statistic returned:
 *	def pl = new ProgressLogger()
 * [1, 2, 3, 4].each{
 *	sleep 1000;
 *	pl.next();
 * }
 * Result will be something like:
 * Process [item] #1. Spent (pack by 1) time: 1,007 (from start: 1,007)
 * Process [item] #2. Spent (pack by 1) time: 1,000 (from start: 2,055)
 * Process [item] #3. Spent (pack by 1) time: 1,001 (from start: 3,058)
 * Process [item] #4. Spent (pack by 1) time: 1,001 (from start: 4,061)
 *
 * 6) Just for the simplicity it may be:
 * <code>
 *	def pl = new ProgressLogger()
 * [1, 2, 3, 4].each{
 *	pl.next {sleep 1000};
 * }
 *</code>
 *
 * You are able provide result messages, custom message formatting, pack size after precessing write log, auto adjusting
 * such pack size to do not spam log, provide custom logging methods and so on.
 *
 * @author: Pavel Alexeev <Pahan@Hubbitus.info>
 * @created: 02.01.2012 22:01
 */
class ProgressLogger {
	/**
	 * You may with configure prefix to start line from "Run", 'Testing', 'Run test' and so on
	 */
	String processName = 'Process';

	/**
	 * Format of messages. Allow to redefine, customize and i18n
	 */
	static Map FORMAT = [
		item: 'item'
		,progress: '''${processName} [${objectName}] #${currentElementNo} from ${totalAmountOfElements} (${sprintf('%.2f', percentComplete)}%). Spent (pack by ${packLogSize}) time: ${lastPackSpent} (from start: ${fromStartSpent})${( (totalAmountOfElements - currentElementNo && currentElementNo > 1) ? '. ' + _FORMAT.estimation.make(totalAmountOfElements: totalAmountOfElements, currentElementNo: currentElementNo, estimationTimeToFinish: estimationTimeToFinish) : '' )}'''
		,progress_total_unknown: '''${processName} [${objectName}] #${currentElementNo}. Spent (pack by ${packLogSize}) time: ${lastPackSpent} (from start: ${fromStartSpent})'''
		,estimation: 'Estimated items: ${totalAmountOfElements - currentElementNo}, time: ${estimationTimeToFinish}'
		,stop: 'Stop processing [${objectName}] (Total processed ${totalItems}). Spent: ${spent}.${additionalResultInformation ? " " + additionalResultInformation : "" }'
	];

	// Just cache created template as it used many times - and do not use @Lazy transformation to on the fly create items, and do not define it each time format added
	private static Map<String,Template> _FORMAT = [:].withDefault{key->
		new SimpleTemplateEngine().createTemplate((String)FORMAT."$key")
	}

	boolean autoAdjust = false;
	int autoAdjustFactor = 50; // Roughly packLogSize 2% initially

	private long start;
	private long last;
	private long totalAmountOfElements = -1;
	int packLogSize;
	String objName;
	private AtomicLong current = new AtomicLong(0);
	/** Consumer nor Closure to interoperability with pure Java uses! **/
	private Consumer outMethod;

	long lastPackSpentNs;
	long spentFromStartNs;
	long leaved;

	/**
	 * Constructor from known amount of executions
	 *
	 * @param totalAmountOfElements By default value -1 mean what total number elements is not known (f.e. tree traversal or stream
	 *	processing) in that case simpler statistics printed without totals and estimation
	 * @param outMethod
	 * @param packLogSize If null - auto adjust step. By default amount of object divided on 100 (100%, 1% at step) and then if
	 *	such part will be executed faster than in second - redivide to 100 to decrease log overhead.
	 * @param objName {@see FORMAT.item} by default
	 */
	ProgressLogger(long totalAmountOfElements = -1, Consumer outMethod = {println it}, Integer packLogSize = null, String objName = null){
		this.totalAmountOfElements = totalAmountOfElements;
		this.objName = (objName ?: FORMAT.item);
		this.outMethod = outMethod;
		if (!packLogSize){
			this.autoAdjust = true;
			autoAdjustEach();
		}
		else{
			this.packLogSize = packLogSize;;
		}
		reset()
	}

	/**
	 * Create from list
	 *
	 * @param list
	 * @param outMethod
	 * @param objName If omitted - class.simpleName of first list element used if it exists ('empty list' if list empty)
	 * @param packLogSize
	 */
	ProgressLogger(Collection list, Consumer outMethod = {println it}, String objName = null, Integer packLogSize = null){
		this( (list?.size() ?: 0), outMethod, packLogSize, (objName ?: (list?.size() ? list.get(0)?.getClass()?.simpleName : 'empty list')) );
	}

	/**
	 * Reset timers
	 */
	void reset(){
		this.last = this.start = System.nanoTime();
	}

	/**
	 * Intended to be main method to process collections:
	 * ProgressLogger.packLogSize([1, 2, 3]){
	 *	sleep 2000; // long operation
	 *	println it;
	 * }
	 *
	 * @param list
	 * @param doing
	 * @param outMethod
	 * @param objName
	 * @param each
	 */
	static void each(Collection list, Consumer doing, Consumer outMethod = {println it}, String objName = null, Integer each = null){
		ProgressLogger pl = new ProgressLogger(list, outMethod, objName, each);
		list.each{
			pl.next();
			doing.accept(it);
		}
	}

	/**
	 * Static method to measure one execution of some peace code.
	 * String from closure execution (called {@link #toString()}) result will be placed into Spent.info
	 *
	 * @param outMethod How to log result
	 * @param execute Supplier (Closure) to execute. None arguments.
	 * @param objName optional objects common nape present in logging
	 * @param beginMessage optional string on start process
	 * @param additionalEndMessage if it non-empty it will be Spent.info, otherwise result of execution
	 * @return
	 */
	static Spent measure(Consumer outMethod, Supplier execute, String objName = '', String beginMessage = null, String additionalEndMessage = ''){
		if (beginMessage) outMethod.accept(beginMessage);
		ProgressLogger pl = new ProgressLogger(1, outMethod, -1, '');
		if (objName) pl.objName = objName;
		def execRes = execute.get();
		Spent spent = pl.stop(additionalEndMessage ?: execRes);
		spent.info = (additionalEndMessage ?: execRes);
		return spent;
	}

	/**
	 * Main method to log message
	 *
	 * @param currentElementNo
	 */
	private void logProgress(long currentElementNo, String iterationName = null){
		lastPackSpentNs = System.nanoTime() - last;
		spentFromStartNs = System.nanoTime() - start;
		leaved = this.totalAmountOfElements - currentElementNo;

		if ( ! (currentElementNo % packLogSize) || currentElementNo == this.totalAmountOfElements || 1 == currentElementNo){ // First, last and by pack of 'packLogSize' amount of elements
			outMethod.accept(
				(-1L == totalAmountOfElements ? _FORMAT.progress_total_unknown : _FORMAT.progress).make(
					processName: processName
					,objectName: iterationName ?: objName
					,currentElementNo: currentElementNo
					,totalAmountOfElements: this.totalAmountOfElements
					,percentComplete: currentElementNo / this.totalAmountOfElements * 100
					,packLogSize: packLogSize
					,lastPackSpent: Spent.formatTimeElapsedSinceNanosecond(lastPackSpentNs)
					,fromStartSpent: Spent.formatTimeElapsedSinceNanosecond(spentFromStartNs)
					,estimationTimeToFinish: Spent.formatTimeElapsedSinceNanosecond( (spentFromStartNs / currentElementNo * (this.totalAmountOfElements - currentElementNo)).toLong() )
					,_FORMAT: _FORMAT // For Estimation online add
				).toString() // .toString for compatibility with Java methods, passed by ref
			);
			last = System.nanoTime();
			autoAdjust();
		}
	}

	/**
	 * For use just as time measure.
	 * Assumed only once call after object creation.
	 *
	 * @param additionalResultInformation - Optional information to include in stop message. Useful for short messages like: "Got N objects" or "Result archive size is M"
	 * @return
	 */
	Spent stop(String additionalResultInformation = ''){
		Spent spent = new Spent(System.nanoTime() - start);

		outMethod.accept(_FORMAT.stop.make(objectName: objName, spent: spent, additionalResultInformation: additionalResultInformation, totalItems: current.incrementAndGet()).toString());
		spent;
	}

	/**
	 * Tick on packLogSize element
	 * It may be used just like ++ operator:
	 * <code>
	 *	def pl = new ProgressLogger()
	 * [1, 2, 3, 4].each{
	 *	sleep 1000;
	 *	pl.next();
	 * }
	 * </code>
	 * <code>
	 *	def pl = new ProgressLogger()
	 * [1, 2, 3, 4].each{
	 *	++pl.next();
	 * }
	 * </code>
	 */
	void next(){
		logProgress(current.incrementAndGet());
	}

	/**
	 * Just syntax sugar for compact simple operations.
	 * Instead of:
	 * <code>
	 *	def pl = new ProgressLogger()
	 * [1, 2, 3, 4].each{
	 *	sleep 1000;
	 *	pl.next();
	 * }
	 * </code>
	 * Just for the simplicity it may be used as:
	 * <code>
	 *	def pl = new ProgressLogger()
	 *	[1, 2, 3, 4].each{
	 *		pl.next {sleep 1000};
	 *	}
	 *</code>
	 *
	 * @param toRun {@see Supplier} instead of {@see Closure} uses to compatibility with Java calls
	 * @return
	 */
	def next(Supplier toRun){
		def res = toRun.get()
		logProgress(current.incrementAndGet())
		return res
	}

	/**
	 * Method to tick like:
	 *
	 *	def pl = new ProgressLogger()
	 * ['one', 'two', 'three', 'four'].each{
	 *	pl.next it
	 * }
	 *
	 * Output will be something like:
	 * Process [one] #1. Spent (pack by 1) time: 1,007 (from start: 1,007)
	 * Process [two] #1. Spent (pack by 1) time: 1,007 (from start: 1,007)
	 * Process [three] #1. Spent (pack by 1) time: 1,007 (from start: 1,007)
	 * Process [four] #1. Spent (pack by 1) time: 1,007 (from start: 1,007)
	 *
	 * @param iterationName
	 */
	void next(String iterationName){
		logProgress(current.incrementAndGet(), iterationName);
	}

	/**
	 * Adjust elements batch size automatically to do not SPAM log
	 */
	synchronized void autoAdjust(){
		if (autoAdjust && current.get() > 2 && lastPackSpentNs / (10 ** 9) < 1){
			this.autoAdjustFactor = 10;
			autoAdjustEach();
		}
	}

	synchronized private void autoAdjustEach(){
		this.packLogSize = this.totalAmountOfElements / autoAdjustFactor;
			if (packLogSize in [0, 1]){
				packLogSize = 1;
				return;
			}
		int roundFactor = 10 ** (Math.ceil(Math.log10(packLogSize)) - 1);
//		println "Adjusting: ${autoAdjust}; autoAdjustFactor=${autoAdjustFactor}; current=${current}; lastPackSpentNs(sec)=${lastPackSpentNs / (10 ** 9)}; packLogSize=${packLogSize}; roundFactor=${roundFactor}";
		// Round up to 10 ** N - 1. F.e. 15 will be rounded to 20, 234 to 300, 5678 to 5000
		this.packLogSize = ( Math.ceil(packLogSize / roundFactor) * roundFactor ?: 1 );
	}
}
