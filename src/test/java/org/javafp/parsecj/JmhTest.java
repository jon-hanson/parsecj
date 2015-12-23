package org.javafp.parsecj;

import org.javafp.parsecj.expr2.GrammarTest;
import org.junit.Test;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.IOException;

public final class JmhTest {
/* perf testing disabled.
    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class ExprState {
        public String getGoodExpr() {
            return "zap(1.23*-max(4.567%+(890.12bp+-x),-2.3bp)-1,3*-max(1e2%+(5bp+-x),-2bp)-1)";
        }

        public String getBadExpr() {
            return "zap(1.23*-max(4.567%+(890.12bp+-x),-2.3bp)-1,3*-max(1e2%+(5bp+-x),-2bp)-1)(";
        }
    }

    @Test
    public void runJmh() throws RunnerException, IOException {

        final Options opt = new OptionsBuilder()
            .jvmArgs("-XX:+UnlockCommercialFeatures")
                .include(GrammarTest.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();
           new Runner(opt).run();
      }
*/
}
