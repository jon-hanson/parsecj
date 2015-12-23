package org.javafp.parsecj;

import org.javafp.data.Unit;
import org.junit.*;

import static org.javafp.parsecj.Combinators.*;
import static org.javafp.parsecj.Text.*;

import static org.javafp.parsecj.TestUtils.*;

public class TextTest {

    private static final Parser<Character, Unit> eof = eof();

    @Test
    public void testAlpha() throws Exception {
        assertParserFails(alpha, "\0");
        assertParserFails(alpha, "0");
        assertParserFails(alpha, "9");
        assertParserFails(alpha, "!");
        assertParserFails(alpha, " ");
        assertParserFails(alpha, ",");

        assertParserSucceedsWithValue(alpha, "a", 'a');
        assertParserSucceedsWithValue(alpha, "z", 'z');
        assertParserSucceedsWithValue(alpha, "A", 'A');
        assertParserSucceedsWithValue(alpha, "Z", 'Z');
    }

    @Test
    public void testDigit() throws Exception {
        assertParserFails(digit, "\0");
        assertParserFails(digit, "a");
        assertParserFails(digit, "A");
        assertParserFails(digit, "!");
        assertParserFails(digit, " ");
        assertParserFails(digit, ",");

        assertParserSucceedsWithValue(digit, "0", '0');
        assertParserSucceedsWithValue(digit, "9", '9');
    }

    @Test
    public void testSpace() throws Exception {
        assertParserFails(space, "\r");
        assertParserFails(space, "\n");
        assertParserFails(space, "\t");
        assertParserFails(space, "\0");
        assertParserFails(space, "0");
        assertParserFails(space, "a");
        assertParserFails(space, ",");

        assertParserSucceedsWithValue(space, " ", ' ');
    }

    @Test
    public void testWSpace() throws Exception {
        assertParserFails(wspace, "\0");
        assertParserFails(wspace, "0");
        assertParserFails(wspace, "a");
        assertParserFails(wspace, ",");

        assertParserSucceedsWithValue(wspace, " ", ' ');
        assertParserSucceedsWithValue(wspace, "\r", '\r');
        assertParserSucceedsWithValue(wspace, "\n", '\n');
        assertParserSucceedsWithValue(wspace, "\t", '\t');
    }

    @Test
    public void testWSpaces() throws Exception {
        final Parser<Character, Unit> p = wspaces.then(eof);

        assertParserFails(p, "A ");
        assertParserFails(p, " A");
        assertParserFails(p, " A ");

        assertParserSucceeds(p, " ");
        assertParserSucceeds(p, " \t\n\r ");
    }

    @Test
    public void testChr() throws Exception {
        final Parser<Character, Character> p = chr('X');

        assertParserFails(p, "A");
        assertParserFails(p, "AX");
        assertParserFails(p, "x");
        assertParserFails(p, " X");

        assertParserSucceeds(p, "X");
    }

    @Test
    public void testInteger() throws Exception {
        final Parser<Character, Integer> p = intr.bind(i -> eof.then(retn(i)));

        assertParserFails(p, "");
        assertParserFails(p, "+");
        assertParserFails(p, "-");
        assertParserFails(p, "1.1");
        assertParserFails(p, "+-1");
        assertParserFails(p, "0-0");
        assertParserFails(p, "0+0");
        assertParserFails(p, "+0+");
        assertParserFails(p, "1 0");
        assertParserFails(p, "0 1");
        assertParserFails(p, "1e");
        assertParserFails(p, "e1");
        assertParserFails(p, "1.0");
        assertParserFails(p, "0.1");
        assertParserFails(p, "1234.5678");
        assertParserFails(p, "1234e10");
        assertParserFails(p, "e1");

        assertParserSucceedsWithValue(p, "0", 0);
        assertParserSucceedsWithValue(p, "1", 1);
        assertParserSucceedsWithValue(p, "-1", -1);
        assertParserSucceedsWithValue(p, "123456789", 123456789);
        assertParserSucceedsWithValue(p, "-123456789", -123456789);
    }

    @Test
    public void testLong() throws Exception {
        final Parser<Character, Long> p = lng.bind(l -> eof.then(retn(l)));

        assertParserFails(p, "");
        assertParserFails(p, "+");
        assertParserFails(p, "-");
        assertParserFails(p, "1.1");
        assertParserFails(p, "+-1");
        assertParserFails(p, "0-0");
        assertParserFails(p, "0+0");
        assertParserFails(p, "+0+");
        assertParserFails(p, "1 0");
        assertParserFails(p, "0 1");
        assertParserFails(p, "1e");
        assertParserFails(p, "e1");
        assertParserFails(p, "1.0");
        assertParserFails(p, "0.1");
        assertParserFails(p, "1234.5678");
        assertParserFails(p, "1234e10");
        assertParserFails(p, "e1");

        assertParserSucceedsWithValue(p, "0", 0l);
        assertParserSucceedsWithValue(p, "1", 1l);
        assertParserSucceedsWithValue(p, "-1", -1l);
        assertParserSucceedsWithValue(p, "123456789", 123456789l);
        assertParserSucceedsWithValue(p, "-123456789", -123456789l);
        assertParserSucceedsWithValue(p, "4294967294", 4294967294l);
        assertParserSucceedsWithValue(p, "-4294967294", -4294967294l);
    }

    @Test
    public void testNumber() throws Exception {
        final Parser<Character, Number> p = number.bind(l -> eof.then(retn(l)));

        assertParserFails(p, "");
        assertParserFails(p, "+");
        assertParserFails(p, "-");
        assertParserFails(p, "1.1.");
        assertParserFails(p, "+-1");
        assertParserFails(p, "e");
        assertParserFails(p, "0-0");
        assertParserFails(p, "0+0");
        assertParserFails(p, "+0+");
        assertParserFails(p, "1 0");
        assertParserFails(p, "0 1");
        assertParserFails(p, "1e");
        assertParserFails(p, "e1");

        assertParserSucceedsWithValue(p, "0", 0l);
        assertParserSucceedsWithValue(p, "1", 1l);
        assertParserSucceedsWithValue(p, "-1", -1l);
        assertParserSucceedsWithValue(p, "123456789", 123456789l);
        assertParserSucceedsWithValue(p, "-123456789", -123456789l);
        assertParserSucceedsWithValue(p, "4294967294", 4294967294l);
        assertParserSucceedsWithValue(p, "-4294967294", -4294967294l);

        assertParserSucceedsWithValue(p, "0", 0l);
        assertParserSucceedsWithValue(p, "0.", 0l);
        assertParserSucceedsWithValue(p, ".0", 0l);
        assertParserSucceedsWithValue(p, "0.0", 0l);
        assertParserSucceedsWithValue(p, ".1", 0.1);
        assertParserSucceedsWithValue(p, "1", 1l);
        assertParserSucceedsWithValue(p, "1.0", 1l);
        assertParserSucceedsWithValue(p, "1.2", 1.2);
        assertParserSucceedsWithValue(p, "-1.2", -1.2);
        assertParserSucceedsWithValue(p, "123456789.123456789", 123456789.123456789);
        assertParserSucceedsWithValue(p, "-123456789.123456789", -123456789.123456789);

        assertParserSucceedsWithValue(p, "12345.6789e12", 12345678900000000l);
        assertParserSucceedsWithValue(p, "-12345.6789e12", -12345678900000000l);
        assertParserSucceedsWithValue(p, "12345.6789e-12", 12345.6789e-12);
        assertParserSucceedsWithValue(p, "-12345.6789e-12", -12345.6789e-12);

        assertParserSucceedsWithTest(p, "9e99999999", n -> Double.isInfinite((Double)n));
        assertParserSucceedsWithTest(p, "-9e99999999", n -> Double.isInfinite((Double) n));
    }

    @Test
    public void testDouble() throws Exception {
        final Parser<Character, Double> p = dble.bind(d -> eof.then(retn(d)));

        assertParserFails(p, "");
        assertParserFails(p, "+");
        assertParserFails(p, "-");
        assertParserFails(p, "1.1.");
        assertParserFails(p, "+-1");
        assertParserFails(p, "e");
        assertParserFails(p, "0-0");
        assertParserFails(p, "0+0");
        assertParserFails(p, "+0+");
        assertParserFails(p, "1 0");
        assertParserFails(p, "0 1");
        assertParserFails(p, "1e");
        assertParserFails(p, "e1");

        assertParserSucceedsWithValue(p, "0", 0.0);
        assertParserSucceedsWithValue(p, "0.", 0.0);
        assertParserSucceedsWithValue(p, ".0", 0.0);
        assertParserSucceedsWithValue(p, "0.0", 0.0);
        assertParserSucceedsWithValue(p, ".1", 0.1);
        assertParserSucceedsWithValue(p, "1", 1.0);
        assertParserSucceedsWithValue(p, "1.0", 1.0);
        assertParserSucceedsWithValue(p, "1.2", 1.2);
        assertParserSucceedsWithValue(p, "-1.2", -1.2);
        assertParserSucceedsWithValue(p, "123456789.123456789", 123456789.123456789);
        assertParserSucceedsWithValue(p, "-123456789.123456789", -123456789.123456789);

        assertParserSucceedsWithValue(p, "12345.6789e12", 12345.6789e12);
        assertParserSucceedsWithValue(p, "-12345.6789e12", -12345.6789e12);
        assertParserSucceedsWithValue(p, "12345.6789e-12", 12345.6789e-12);
        assertParserSucceedsWithValue(p, "-12345.6789e-12", -12345.6789e-12);

        assertParserSucceedsWithTest(p, "9e99999999", d -> Double.isInfinite(d));
        assertParserSucceedsWithTest(p, "-9e99999999", d -> Double.isInfinite(d));
    }

    @Test
    public void testStrBetween() throws Exception {
        final Parser<Character, String> p =
            strBetween(chr('['), chr(']')).bind(s ->
                eof.then(retn(s)));

        assertParserFails(p, "");
        assertParserFails(p, "[");
        assertParserFails(p, "]");
        assertParserFails(p, "abd");
        assertParserFails(p, "abc]");
        assertParserFails(p, "[abc");
        assertParserFails(p, "[abc]def");

        assertParserSucceedsWithValue(p, "[]", "");
        assertParserSucceedsWithValue(p, "[abc]", "abc");
    }
}
