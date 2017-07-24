package org.javafp.parsecj;

import org.javafp.parsecj.input.Input;
import org.junit.Assert;

import java.util.function.Predicate;

public class TestUtils {
    private static final double THRESHOLD = 1e-8;

    static <A> void assertParserSucceeds(
            Parser<Character, A> p,
            String input) {
        final Reply<Character, A> reply = p.parse(Input.of(input));
        Assert.assertTrue(
            "Parse of \"" + input + "\"",
            reply.isOk()
        );
    }

    static <A> void assertParserSucceedsWithTest(
        Parser<Character, A> p,
        String input,
        Predicate<A> test) throws Exception {
        final Reply<Character, A> reply = p.parse(Input.of(input));
        Assert.assertTrue(
            "Parse of \"" + input + "\"",
            test.test(reply.getResult())
        );
    }

    static <A> void assertParserSucceedsWithValue(
            Parser<Character, A> p,
            String input,
            A expected) throws Exception {
        final Reply<Character, A> reply = p.parse(Input.of(input));
        Assert.assertEquals(
            "Parse of \"" + input + "\"",
            expected,
            reply.getResult()
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
        final Reply<Character, Double> reply = p.parse(Input.of(input));
        Assert.assertEquals(
            "Parse of \"" + input + "\"",
            expected,
            reply.getResult(),
            THRESHOLD
        );
    }

    static <A> void assertParserFails(
            Parser<Character, A> p,
            String input) throws Exception {
        final Reply<Character, A> reply = p.parse(Input.of(input));
        Assert.assertTrue(
            "Parse of \"" + input + "\"",
            reply.isError()
        );
    }
}
