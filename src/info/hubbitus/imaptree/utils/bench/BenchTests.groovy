package info.hubbitus.imaptree.utils.bench

import groovy.transform.ToString

/**
 * Simple class for represent benchmark tests
 *
 * @author: Pavel Alexeev <Pahan@Hubbitus.info>
 * @created: 02.03.2013 22:27:23
 */
@ToString
class BenchTests{
	List<BenchTest> tests = [];
	Map<BenchTest,BenchResult> results = [:];
//	def data; // Loaded channels itself. Global list

	/**
	 * Run all tests.
	 * Results in results property list
	 *
	 * @return
	 */
	public List<BenchTest> run(){
		run({ true });
	}

	/**
	 * As {@see run()} but accept lists of id tests to run
	 *
	 * @param toRun
	 * @return
	 */
	public List<BenchTest> run(List toRun){
		run({ it.id in toRun });
	}

	/**
	 * As {@see run()} but accept closure, which by provided test object (single parameter) return true if that test
	 *	should be run, or false otherwise
	 *
	 * @param toRun
	 * @return
	 */
	public List<BenchTest> run(Closure toRun){
		tests.each{
			if (toRun(it)){
				results << [ (it): it.run() ];
			}
		}
	}

	public leftShift(BenchTest test){
		tests << test;
	}
}
