package info.hubbitus.imaptree.utils.bench


/**
 * Simple class for represent benchmark test result
 *
 * @author: Pavel Alexeev <Pahan@Hubbitus.info>
 * @created: 02.03.2013 22:27:23
 */
public class BenchResult{
	Spent min;
	Spent avg;
	Spent max;
	Spent sum;

	public String toString(){
		['min', 'avg', 'max', 'sum'].collect{ "${it}=${this."$it"}" }.join('\n')
	}
}
