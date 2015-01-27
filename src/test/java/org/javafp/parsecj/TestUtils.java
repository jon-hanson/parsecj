package org.javafp.parsecj;

import org.junit.Assert;

import java.util.function.Predicate;

public class TestUtils {
    private static final double THRESHOLD = 1e-8;

    static <A> void assertParserSucceeds(
            Parser<Character, A> p,
            String input) {
        Assert.assertTrue(
            "Parse of \"" + input + "\"",
            p.parse(State.state(input)).isOk()
        );
    }

    static <A> void assertParserSucceeds(
            Parser<Character, A> p,
            String input,
            Predicate<A> test) throws Exception {
        Assert.assertTrue(
            "Parse of \"" + input + "\"",
            test.test(p.parse(State.state(input)).getResult())
        );
    }

    static <A> void assertParserSucceedsWithValue(
            Parser<Character, A> p,
            String input,
            A expected) throws Exception {
        Assert.assertEquals(
            "Parse of \"" + input + "\"",
            expected,
            p.parse(State.state(input)).getResult()
        );
    }

    static void assertParserSucceedsWithValue(
            Parser<Character, String> p,
            String input) throws Exception {
        assertParserSucceedsWithValue(p, input, input);
    }

    static void assertParserSucceedsWithValue(
            Parser<Character, Double> p,
            String input,
            Double expected) throws Exception {
        Assert.assertEquals(
            "Parse of \"" + input + "\"",
            expected,
            p.parse(State.state(input)).getResult(),
            THRESHOLD
        );
    }

    static <A> void assertParserFails(
            Parser<Character, A> p,
            String input) {
        Assert.assertTrue(
            "Parse of \"" + input + "\"",
            p.parse(State.state(input)).isError()
        );
    }
}
