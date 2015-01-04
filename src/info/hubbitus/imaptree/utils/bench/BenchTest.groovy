package info.hubbitus.imaptree.utils.bench

import groovy.transform.ToString
import groovy.util.logging.Log4j

/**
 * Simple class for represent benchmark test
 *
 * @author: Pavel Alexeev <Pahan@Hubbitus.info>
 * @created: 02.03.2013 22:27:23
 */
@Log4j
@ToString
class BenchTest{
	static DEFAULT_AMOUNT_OF_RUN = 10;

	String id;
	String name;
	int amountOfRun;

	/**
	 * Once bootstrap. Optional.
	 * Share context this of that class which allow save state (e.g. Declare and init vars, access out method and so on)
	 * (http://hamletdarcy.blogspot.ru/2007/11/fun-with-groovy-closures-variable-scope.html)
	 * WARNING! To init var which should be then available in {@see runBody()} you must define it without type specification:
	 * String str = 'Some string';
	 * will NOT work, instead use just:
	 * str = 'Some string';
	 */
	Closure runPrepare = {};
	/**
	 * Main horse of test. Logic to repeat {@see amountOfRun} times
	 */
	Closure runBody;
	/**
	 * Method of out progress of test. Optional. By default log.info
	 */
	Closure out = log.&info;

	BenchResult res;

	public BenchResult run(){
		out("Run test $id: $name");

		if (!amountOfRun) amountOfRun = DEFAULT_AMOUNT_OF_RUN;

		runPrepare.delegate = this;
		runPrepare();
		runBody.delegate = this;

		res = Benchmark.benchmark(runBody, amountOfRun, this.&out)
	}
}
