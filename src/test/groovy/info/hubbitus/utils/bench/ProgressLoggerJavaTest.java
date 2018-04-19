package info.hubbitus.utils.bench;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import groovy.util.logging.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;

/**
 * @author Pavel Alexeev.
 * @since 2017-09-17 17:21.
 */
@Slf4j
public class ProgressLoggerJavaTest extends Assert {
    private final StringBuffer sb = new StringBuffer();
    private Consumer bufferWrite = (it) -> sb.append(it).append('\n');

    @Test
    public void testWithImplicitCreateConsumer(){
        List list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        ProgressLogger pl = new ProgressLogger(
            list
            ,new Consumer() {
                @Override
                public void accept(Object it) {
                    System.out.println(it);
                    bufferWrite.accept(it);
                }
            }
        );
        list.forEach( (t) -> pl.next() );

        assertThat(sb.toString(), matchesPattern("(?ms)^Process \\[Integer\\] #1 from 10 \\(10[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\n" +
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
    public void testWithLambda(){
        List list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        ProgressLogger pl = new ProgressLogger(
            list
            , it -> bufferWrite.accept(it)
        );
        list.forEach( (t) -> pl.next() );

        assertThat(sb.toString(), matchesPattern("(?ms)^Process \\[Integer\\] #1 from 10 \\(10[,.]00%\\)\\. Spent \\(pack by 1\\) time: \\d+[,.]\\d{3} \\(from start: \\d+[,.]\\d{3}\\)\n" +
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

}
