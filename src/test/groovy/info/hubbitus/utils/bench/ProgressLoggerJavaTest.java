package info.hubbitus.utils.bench;

import java.util.List;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static java.util.Arrays.asList;

/**
 * @author Pavel Alexeev.
 * @since 2017-09-17 17:21.
 */
@Slf4j
public class ProgressLoggerJavaTest extends Assert {
	private StringBuffer sb;
	private final Consumer<Object> bufferWrite = (it) -> sb.append(it).append('\n');

	@Before
	public void bufferInit(){
		sb = new StringBuffer();
	}

	@Test
	public void testWithImplicitCreateConsumer(){
		List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		ProgressLogger pl = new ProgressLogger(
			list
			, it -> {
				System.out.println(it);
				bufferWrite.accept(it);
			}
		);
		list.forEach( (t) -> pl.next() );

		MatcherAssert.assertThat(sb.toString(), matchesPattern("(?ms)^Process \\[Integer\\] #1 from 10 \\(10[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\n" +
			"Process \\[Integer\\] #2 from 10 \\(20[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 8, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #3 from 10 \\(30[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 7, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #4 from 10 \\(40[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 6, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #5 from 10 \\(50[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 5, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #6 from 10 \\(60[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 4, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #7 from 10 \\(70[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 3, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #8 from 10 \\(80[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 2, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #9 from 10 \\(90[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 1, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #10 from 10 \\(100[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\n")
		);
	}

	@Test
	public void testWithLambdaConsumer(){
		List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		ProgressLogger pl = new ProgressLogger(
			list
			,bufferWrite
		);
		list.forEach( (t) -> pl.next() );

		MatcherAssert.assertThat(sb.toString(), matchesPattern("(?ms)^Process \\[Integer\\] #1 from 10 \\(10[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\n" +
			"Process \\[Integer\\] #2 from 10 \\(20[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 8, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #3 from 10 \\(30[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 7, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #4 from 10 \\(40[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 6, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #5 from 10 \\(50[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 5, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #6 from 10 \\(60[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 4, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #7 from 10 \\(70[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 3, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #8 from 10 \\(80[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 2, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #9 from 10 \\(90[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 1, time: \\d+[,.]\\d{3}\n" +
			"Process \\[Integer\\] #10 from 10 \\(100[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\n")
		);
	}

	@Test
	public void measureAndLogTimeTest(){
		long result = (long)ProgressLogger.measureAndLogTime(
			spentTime-> bufferWrite.accept("Operation took: " + spentTime),
			() -> {
				System.out.println ("test"); // Some long measured work
				return 42L;
			}
		);
		assertEquals(42, result);
		MatcherAssert.assertThat(sb.toString(), matchesPattern("Operation took: 0[,.]\\d{3}\n"));
	}

	@Test
	public void listProcessWithLombokLog(){
		List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		ProgressLogger pl = new ProgressLogger(list, (Consumer<String>)(log::info));
		list.forEach( (t) -> pl.next() );
	}

	@Test
	public void listEachStatic(){
		ProgressLogger.each(asList("one", "two", 3, 4, 5), (it) -> {
			// Useful operation on each element of list
			System.out.println(it);
			bufferWrite.accept(it);
		}, (Consumer<String>)log::info
		,null
		,1
		);

		assertEquals(sb.toString(), "one\ntwo\n3\n4\n5\n");
	}

	@Test
	public void listEachStaticDynamicItemName(){
		ProgressLogger.each(asList("one", "two", 3, 4, 5), (it) -> {
			// Useful operation on each element of list
			System.out.println("Process: " + it);
		}, bufferWrite, "item <%s>");

		MatcherAssert.assertThat(sb.toString(), matchesPattern("(?ms)^Process \\[item <one>\\] #1 from 5 \\(20[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\n" +
			"Process \\[item <two>\\] #2 from 5 \\(40[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 3, time: \\d+[,.]\\d{3}\n" +
			"Process \\[item <3>\\] #3 from 5 \\(60[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 2, time: \\d+[,.]\\d{3}\n" +
			"Process \\[item <4>\\] #4 from 5 \\(80[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\\. Estimated items: 1, time: \\d+[,.]\\d{3}\n" +
			"Process \\[item <5>\\] #5 from 5 \\(100[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\n")
		);
	}
}
