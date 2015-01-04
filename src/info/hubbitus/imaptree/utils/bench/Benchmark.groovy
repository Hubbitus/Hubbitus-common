package info.hubbitus.imaptree.utils.bench

/**
 * Simple class for benchmarking operations. F.e. to compare implementations
 *
 * Born in ais adapter.
 *
 * @author: Pavel Alexeev <Pahan@Hubbitus.info>
 * @created: 02.03.2013 22:27:23
 */
class Benchmark{
	/**
	 * Execute count times run closure and return timing statistics in map
	 *
	 * @param run
	 * @param count
	 * @param outMethod
	 * @return
	 */
	public static BenchResult benchmark(Closure run, int count, Closure outMethod = {}){
		List<Spent> res = [];
		ProgressLogger pl = new ProgressLogger(1, {}, -1, '');
		count.times{
			run();
			res << pl.stop();
			pl.reset();
		}
		BenchResult result = new BenchResult( min: res.min{Spent it-> it.spent}, max: res.max{Spent it-> it.spent}, avg: new Spent((Long)res.sum{Spent it-> it.spent}/count), sum: new Spent((Long)res.sum{Spent it-> it.spent}) );
		outMethod( _('Benchmarking %d iterations done', count) + '; Res:\n' + result );
		return result;
	}
}



