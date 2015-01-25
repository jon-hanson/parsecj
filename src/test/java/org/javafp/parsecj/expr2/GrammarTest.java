package org.javafp.parsecj.expr2;

import org.javafp.parsecj.JmhTest;
import org.javafp.parsecj.Reply;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

public class GrammarTest {

    private static void assertSuccess(String input, String expected) throws Exception {
        final String result = Grammar.parse(input).getResult().toString();
        Assert.assertEquals(expected, result);
        Assert.assertEquals(expected, Grammar.parse(result).getResult().toString());
    }

    private static void assertFailure(String input, String errorMsg) {
        Grammar.parse(input).match(
            ok -> {
                throw new RuntimeException("Expected parse to fail");
            },
            error -> {
                Assert.assertEquals(errorMsg, error.getMsg());
                return null;
            }
        );
    }

    @Test
    public void testSuccess() throws Exception {
        assertSuccess("3*-max(4%+(5bp+-x),-2bp)-1", "(3.0*-((max((4.0%+(5.0bp+-(x))),-2.0bp)-1.0)))");
    }

    @Test
    public void testFailure() throws Exception {
        assertFailure("3*-max(4%+(5bp+),-2bp)-1", "Unexpected ')' at position 15. Expecting one of [num,brack-expr,alphaNum,+,-]");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public Reply<Character, Model.Expr> benchmarkSuccess(JmhTest.ExprState state) {
        try {
            return Grammar.parse(state.getGoodExpr());
        } catch (Exception ex) {
            return null;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public Reply<Character, Model.Expr> benchmarkFailure(JmhTest.ExprState state) {
        try {
            return Grammar.parse(state.getBadExpr());
        } catch (Exception ex) {
            return null;
        }
    }
}
