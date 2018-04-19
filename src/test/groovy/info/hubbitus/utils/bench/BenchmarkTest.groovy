package info.hubbitus.utils.bench

import spock.lang.Specification

/**
 * @author Pavel Alexeev.
 * @since 2017-10-26 21:54.
 */
class BenchmarkTest extends Specification {
    def "test benchmark"() {
        when:
            BenchResult res = Benchmark.benchmark({ 3 * 5 }, 100)
        then:
            noExceptionThrown()
            res
    }

    def "test benchmark with writer"() {
        given:
            StringBuffer sb = new StringBuffer()
        when:
            BenchResult res = Benchmark.benchmark({ 3 * 5 }, 100, {it-> sb.append(it)})
        then:
            noExceptionThrown()
            sb.toString() ==~ /Benchmarking 100 iterations done; Res:\n min=0[,.]\d{3}\navg=0[,.]\d{3}\nmax=0[,.]\d{3}\nsum=0[,.]\d{3}/
            res
    }
}
